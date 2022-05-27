package com.octopus.octopusproxy.domain.handlers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.google.common.base.Preconditions;
import com.octopus.encryption.AsymmetricDecryptor;
import com.octopus.exceptions.EntityNotFoundException;
import com.octopus.exceptions.InvalidFilterException;
import com.octopus.exceptions.JsonSerializationException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.features.AdminJwtClaimFeature;
import com.octopus.features.AdminJwtGroupFeature;
import com.octopus.features.MicroserviceNameFeature;
import com.octopus.jwt.JwtInspector;
import com.octopus.jwt.JwtUtils;
import com.octopus.octopusproxy.domain.entities.Space;
import com.octopus.octopusproxy.domain.entities.SpaceCollection;
import com.octopus.octopusproxy.domain.features.ClientPrivateKey;
import com.octopus.octopusproxy.domain.features.impl.DisableSecurityFeatureImpl;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import io.quarkus.logging.Log;
import io.vavr.control.Try;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Handlers take the raw input from the upstream service, like Lambda or a web server, convert the
 * inputs to POJOs, apply the security rules, and then pass the requests down to repositories.
 */
@ApplicationScoped
public class ResourceHandler {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Inject
  AdminJwtClaimFeature adminJwtClaimFeature;

  @ConfigProperty(name = "cognito.client-id")
  String cognitoClientId;

  @Inject
  DisableSecurityFeatureImpl cognitoDisableAuth;

  @Inject
  AdminJwtGroupFeature adminJwtGroupFeature;

  @Inject
  ResourceConverter resourceConverter;

  @Inject
  JwtInspector jwtInspector;

  @Inject
  JwtUtils jwtUtils;

  @Inject
  ClientPrivateKey clientPrivateKey;

  @Inject
  AsymmetricDecryptor asymmetricDecryptor;

  @Inject
  MicroserviceNameFeature microserviceNameFeature;

  /**
   * Returns the one resource that matches the supplied ID.
   *
   * @param id                   The ID of the resource to return.
   * @param dataPartitionHeaders The "data-partition" headers.
   * @return The matching resource.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *                                        resource.
   */
  public String getOne(@NonNull final String id,
      @NonNull final String apiKey,
      @NonNull final List<String> dataPartitionHeaders,
      final String authorizationHeader,
      final String serviceAuthorizationHeader)
      throws DocumentSerializationException {
    Preconditions.checkArgument(StringUtils.isNotBlank(apiKey), "apiKey can not be a blank string");

    if (!isAuthorized(authorizationHeader, serviceAuthorizationHeader)) {
      throw new UnauthorizedException();
    }

    // The id of a space is a URL, so check that the ID is valid
    final URI sanitizedId = Try.of(() -> URI.create(id))
        /*
         We ignore things like query params and anchors. Ideally clients are not appending these
         things to the URL, but we'll be robust and liberal in what we accept from others.
         */
        .map(u -> URI.create(u.getScheme() + "://" + u.getAuthority() + u.getPath()))
        // If the ID is not a URL, return a 404
        .getOrElseThrow(() -> new EntityNotFoundException());

    // Set the default headers to send to the Octopus instance
    final List<Header> headers = List.of(new BasicHeader(
        "X-Octopus-ApiKey",
        asymmetricDecryptor.decrypt(apiKey, clientPrivateKey.privateKeyBase64().get())));

    // Get the space resource
    final String spaceJson = Try.withResources(
            () -> HttpClients.custom().setDefaultHeaders(headers).build())
        .of(client -> {
          final HttpResponse response = client.execute(new HttpGet(sanitizedId));
          if (response.getStatusLine().getStatusCode() == 200) {
            return EntityUtils.toString(response.getEntity());
          }
          throw new RuntimeException();
        })
        // Log any network errors
        .onFailure(e -> Log.error(microserviceNameFeature.getMicroserviceName() + "-Network-ApiCallFailed", e))
        // assume any error means the entity does not exist
        .getOrElseThrow(e -> new EntityNotFoundException());

    // Convert the response to a space object
    final Space space = Try.of(() -> OBJECT_MAPPER.readValue(spaceJson, Space.class))
        // Log any JSON deserialization errors
        .onFailure(e -> Log.error(microserviceNameFeature.getMicroserviceName() + "-Serialization-FailedToDeserialize", e))
        // Any unexpected fields will result in a 500 error message
        .getOrElseThrow(e -> new JsonSerializationException());

    // Set the global ID to the resource URL
    space.setId(sanitizedId.toString());

    // Set the instance that the space belongs to
    space.setInstance(sanitizedId.getScheme() + "://" + sanitizedId.getAuthority());

    return respondWithResource(space);
  }

  /**
   * Returns the space from the instance in the filter with the matching name.
   *
   * @param filter               The RSQL query filter.
   * @param dataPartitionHeaders The "data-partition" headers.
   * @return The matching resource.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *                                        resource.
   */
  public String getAll(@NonNull final String apiKey,
      final String filter,
      @NonNull final List<String> dataPartitionHeaders,
      final String authorizationHeader,
      final String serviceAuthorizationHeader)
      throws DocumentSerializationException {
    Preconditions.checkArgument(StringUtils.isNotBlank(apiKey), "apiKey can not be a blank string");

    if (!isAuthorized(authorizationHeader, serviceAuthorizationHeader)) {
      throw new UnauthorizedException();
    }

    final CustomRsqlVisitor visitor = visitRsqlFilter(filter);

    /*
      We require the instance to filter results to, and the name of the space. They must have been
      "anded" together.
     */

    if (StringUtils.isBlank(visitor.getInstanceArgument())
        || StringUtils.isBlank(visitor.getNameArgument())
        || visitor.getAndCount() != 1) {
      throw new InvalidFilterException();
    }

    // The id of a space is a URL, so check that the ID is valid
    final URI sanitizedId = Try.of(() -> URI.create(visitor.getInstanceArgument()))
        /*
         We ignore things like query params and anchors, as well as any additional path element.
         Ideally clients are not appending these things to the URL, but we'll be robust and liberal
         in what we accept from others.
         */
        .map(u -> URI.create(u.getScheme() + "://" + u.getAuthority()))
        // If the ID is not a URL, return a 404
        .getOrElseThrow(() -> new EntityNotFoundException());

    // Set the default headers to send to the Octopus instance
    final List<Header> headers = List.of(new BasicHeader(
        "X-Octopus-ApiKey",
        asymmetricDecryptor.decrypt(apiKey, clientPrivateKey.privateKeyBase64().get())));

    // Get the space resource
    final String spaceJson = Try.withResources(
            () -> HttpClients.custom().setDefaultHeaders(headers).build())
        .of(client -> {
          final HttpResponse response = client.execute(new HttpGet(
              sanitizedId.toString() + "/api/spaces?partialName="
                  + URLEncoder.encode(visitor.getNameArgument(), StandardCharsets.UTF_8.toString())));
          if (response.getStatusLine().getStatusCode() == 200) {
            return EntityUtils.toString(response.getEntity());
          }
          throw new RuntimeException();
        })
        // assume any error means the entity does not exist
        .getOrElseThrow(e -> new EntityNotFoundException());

    // Convert the response to a space object
    final SpaceCollection spaceCollection = Try.of(
            () -> OBJECT_MAPPER.readValue(spaceJson, SpaceCollection.class))
        // Any unexpected fields will result in a 500 error message
        .getOrElseThrow(e -> new JsonSerializationException());

    // The partial name match is not an exact match, so we need to further refine the results.
    final List<Space> items = spaceCollection.getItems()
        .stream()
        .filter(s -> s.getName().equals(visitor.getNameArgument()))
        .collect(Collectors.toList());

    // Each space needs some additional fields added to indicate their global origin.
    items.forEach(space -> {
      // Set the global ID to the resource URL
      space.setId(sanitizedId.toString() + "/api/spaces/" + space.getEntityId());

      // Set the instance that the space belongs to
      space.setInstance(sanitizedId.toString());
    });

    return respondWithResources(items);
  }

  private CustomRsqlVisitor visitRsqlFilter(final String filter) {
    final Node rootNode = new RSQLParser().parse(filter);
    final CustomRsqlVisitor visitor = new CustomRsqlVisitor();
    rootNode.accept(visitor);
    return visitor;
  }

  private String respondWithResource(final Space space)
      throws DocumentSerializationException {
    final JSONAPIDocument<Space> document = new JSONAPIDocument<Space>(space);
    return new String(resourceConverter.writeDocument(document));
  }

  private String respondWithResources(final List<Space> space)
      throws DocumentSerializationException {
    final JSONAPIDocument<List<Space>> document = new JSONAPIDocument<List<Space>>(space);
    return new String(resourceConverter.writeDocumentCollection(document));
  }

  /**
   * Determines if the supplied token grants the required scopes to execute the operation.
   *
   * @param authorizationHeader        The Authorization header.
   * @param serviceAuthorizationHeader The Service-Authorization header.
   * @return true if the request is authorized, and false otherwise.
   */
  private boolean isAuthorized(
      final String authorizationHeader,
      final String serviceAuthorizationHeader) {

    /*
      This method implements the following logic:
      * If auth is disabled, return true.
      * If the Service-Authorization header contains an access token with the correct scope,
        generated by a known app client, return true.
      * If the Authorization header contains a known group, return true.
      * Otherwise, return false.
     */

    if (cognitoDisableAuth.getCognitoAuthDisabled()) {
      return true;
    }

    /*
      An admin scope granted to an access token generated by a known client credentials
      app client is accepted as machine-to-machine communication.
     */
    if (adminJwtClaimFeature.getAdminClaim().isPresent() && jwtUtils.getJwtFromAuthorizationHeader(
            serviceAuthorizationHeader)
        .map(jwt -> jwtInspector.jwtContainsScope(jwt, adminJwtClaimFeature.getAdminClaim().get(),
            cognitoClientId))
        .orElse(false)) {
      return true;
    }

    /*
      Anyone assigned to the appropriate group is also granted access.
     */
    return adminJwtGroupFeature.getAdminGroup().isPresent()
        && jwtUtils.getJwtFromAuthorizationHeader(authorizationHeader)
        .map(jwt -> jwtInspector.jwtContainsCognitoGroup(jwt,
            adminJwtGroupFeature.getAdminGroup().get()))
        .orElse(false);
  }
}
