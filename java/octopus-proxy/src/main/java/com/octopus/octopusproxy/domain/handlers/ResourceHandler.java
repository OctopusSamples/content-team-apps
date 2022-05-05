package com.octopus.octopusproxy.domain.handlers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.google.common.base.Preconditions;
import com.octopus.encryption.AsymmetricDecryptor;
import com.octopus.exceptions.EntityNotFoundException;
import com.octopus.exceptions.JsonSerializationException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.features.AdminJwtClaimFeature;
import com.octopus.features.AdminJwtGroupFeature;
import com.octopus.jwt.JwtInspector;
import com.octopus.jwt.JwtUtils;
import com.octopus.octopusproxy.domain.entities.Space;
import com.octopus.octopusproxy.domain.features.ClientPrivateKey;
import com.octopus.octopusproxy.domain.features.impl.DisableSecurityFeatureImpl;
import io.vavr.control.Try;
import java.net.URI;
import java.util.List;
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
 * inputs to POJOs, apply the security rules, and then pass the requests down
 * to repositories.
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
        asymmetricDecryptor.decrypt(apiKey, clientPrivateKey.privateKeyBase64())));

    // Get the space resource
    final String spaceJson = Try.withResources(() -> HttpClients.custom().setDefaultHeaders(headers).build())
        .of(client -> {
          final HttpResponse response = client.execute(new HttpGet(sanitizedId));
          if (response.getStatusLine().getStatusCode() == 200) {
            return EntityUtils.toString(response.getEntity());
          }
          throw new RuntimeException();
        })
        // assume any error means the entity does not exist
        .getOrElseThrow(e -> new EntityNotFoundException());

    // Convert the response to a space object
    final Space space = Try.of(() -> OBJECT_MAPPER.readValue(spaceJson, Space.class))
        // Any unexpected fields will result in a 500 error message
        .getOrElseThrow(e -> new JsonSerializationException());

    // Set the global ID to the resource URL
    space.setId(sanitizedId.toString());

    return respondWithResource(space);
  }

  private String respondWithResource(final Space space)
      throws DocumentSerializationException {
    final JSONAPIDocument<Space> document = new JSONAPIDocument<Space>(space);
    return new String(resourceConverter.writeDocument(document));
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
        .map(jwt -> jwtInspector.jwtContainsScope(jwt, adminJwtClaimFeature.getAdminClaim().get(), cognitoClientId))
        .orElse(false)) {
      return true;
    }

    /*
      Anyone assigned to the appropriate group is also granted access.
     */
    return adminJwtGroupFeature.getAdminGroup().isPresent() && jwtUtils.getJwtFromAuthorizationHeader(authorizationHeader)
        .map(jwt -> jwtInspector.jwtContainsCognitoGroup(jwt, adminJwtGroupFeature.getAdminGroup().get()))
        .orElse(false);
  }
}
