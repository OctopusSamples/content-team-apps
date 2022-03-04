package com.octopus.serviceaccount.domain.handlers;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.google.common.hash.Hashing;
import com.octopus.exceptions.InvalidInput;
import com.octopus.exceptions.Unauthorized;
import com.octopus.features.AdminJwtClaimFeature;
import com.octopus.features.AdminJwtGroupFeature;
import com.octopus.features.MicroserviceNameFeature;
import com.octopus.jwt.JwtInspector;
import com.octopus.jwt.JwtUtils;
import com.octopus.serviceaccount.domain.entities.CreateServiceAccount;
import com.octopus.serviceaccount.domain.entities.ServiceAccount;
import com.octopus.serviceaccount.domain.features.DisableSecurityFeatureImpl;
import com.octopus.serviceaccount.infrastructure.clients.OctopusClient;
import io.quarkus.logging.Log;
import io.vavr.control.Try;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Provider.Service;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

/**
 * Handlers take the raw input from the upstream service, like Lambda or a web server, convert the
 * inputs to POJOs, apply the security rules, create an audit trail, and then pass the requests down
 * to repositories.
 */
@ApplicationScoped
public class ServiceAccountHandler {

  @Inject
  AdminJwtClaimFeature adminJwtClaimFeature;

  @ConfigProperty(name = "cognito.client-id")
  String cognitoClientId;

  @Inject
  DisableSecurityFeatureImpl cognitoDisableAuth;

  @Inject
  AdminJwtGroupFeature adminJwtGroupFeature;

  @Inject
  MicroserviceNameFeature microserviceNameFeature;

  @Inject
  ResourceConverter resourceConverter;

  @Inject
  JwtInspector jwtInspector;

  @Inject
  JwtUtils jwtUtils;

  /**
   * Creates a new resource.
   *
   * @param document The JSONAPI resource to create.
   * @return The newly created resource
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *                                        resource.
   */
  public String create(
      @NonNull final String document,
      final String authorizationHeader,
      final String serviceAuthorizationHeader,
      final String idToken)
      throws DocumentSerializationException {

    if (!isAuthorized(authorizationHeader, serviceAuthorizationHeader)) {
      throw new Unauthorized();
    }

    try {
      final CreateServiceAccount createServiceAccount = getResourceFromDocument(document);
      final ServiceAccount serviceAccount = sanitizeAccount(
          createServiceAccount.getServiceAccount());
      final URI octopusServerUri = URI.create("https://" + createServiceAccount.getOctopusServer());

      // Perform a login with the id token
      final Response response = logIn(
          octopusServerUri,
          idToken,
          "{}",
          getStateHash("{}"),
          getNonceHash(idToken));

      // Extract the Octopus cookies from the response
      final List<String> cookieHeaders = getCookies(response);

      // Join the cookies back up
      final String cookies = String.join("; ", cookieHeaders);

      // Find the csrf value
      final Optional<String> csrf = getCsrf(cookieHeaders);

      // Create a new service account, passing in the cookies
      final ServiceAccount newServiceAccount = createServiceAccount(
          octopusServerUri,
          serviceAccount,
          cookies,
          csrf.orElse(""));

      return respondWithResource(newServiceAccount);
    } catch (final ClientWebApplicationException ex) {
      Log.error(microserviceNameFeature.getMicroserviceName() + "-ExternalRequest-Failed "
          + ex.getResponse().readEntity(String.class));
      throw new InvalidInput();
    }
  }

  /**
   * Get the CSRF value from the associated cookie.
   */
  private Optional<String> getCsrf(final List<String> cookieHeaders) {
    return cookieHeaders
        .stream()
        .filter(c -> c.startsWith("Octopus-Csrf-Token"))
        .filter(c -> c.split("=").length == 2)
        .map(c -> c.split("=")[1])
        .findFirst();
  }

  /**
   * Extract any useful cookies from the response. Note this is not a robust cookie parser. For
   * example, expiry dates are ignored. We just know the few cookies that Octopus considers
   * important, and ignore the rest.
   */
  private List<String> getCookies(final Response response) {
    return response
        .getHeaders()
        .entrySet()
        .stream()
        .filter(e -> e.getKey().equalsIgnoreCase("set-cookie"))
        .flatMap(e -> e.getValue().stream().map(Object::toString))
        .map(c -> c.split(";")[0])
        .filter(c -> c.split("=")[0].startsWith("Octopus"))
        .toList();
  }

  /**
   * The state hash uses the "OctoState" salt.
   *
   * @param state The state string.
   * @return The slated, hashed, and base64 encoded version of the state.
   */
  private String getStateHash(final String state) {
    final byte[] hash = Hashing.sha256()
        .hashBytes(("OctoState" + state).getBytes(StandardCharsets.UTF_8)).asBytes();
    final String base64encoded = Base64.getEncoder().encodeToString(hash);
    return Try.of(() -> URLEncoder.encode(base64encoded, StandardCharsets.UTF_8.toString()))
        .getOrElse("");
  }

  /**
   * The nonce hash uses the "OctoNonce" salt.
   *
   * @param idToken The id token.
   * @return The slated, hashed, and base64 encoded version of the nonce found from the id token.
   */
  private String getNonceHash(final String idToken) {
    return jwtInspector.getClaim(idToken, "nonce")
        .map(n -> Hashing.sha256()
            .hashBytes(("OctoNonce" + n).getBytes(StandardCharsets.UTF_8)).asBytes())
        .map(s -> Base64.getEncoder().encodeToString(s))
        .map(b -> Try.of(() -> URLEncoder.encode(b, StandardCharsets.UTF_8.toString()))
            .getOrElse(""))
        .orElse("");
  }

  /**
   * This is a workaround to use a REST client with a variable base URL.
   */
  private Response logIn(final URI apiUri, final String idToken, final String state,
      final String stateHash, final String nonceHash) {
    final OctopusClient remoteApi = RestClientBuilder.newBuilder()
        .baseUri(apiUri)
        .build(OctopusClient.class);
    return remoteApi.logIn(idToken, state, "s=" + stateHash + ";n=" + nonceHash);
  }

  /**
   * This is a workaround to use a REST client with a variable base URL.
   */
  private ServiceAccount createServiceAccount(final URI apiUri, final ServiceAccount serviceAccount,
      final String cookies, final String csrf) {
    final OctopusClient remoteApi = RestClientBuilder.newBuilder()
        .baseUri(apiUri)
        .build(OctopusClient.class);
    return remoteApi.createServiceAccount(serviceAccount, cookies, csrf);
  }

  /**
   * Ensure the service account being created has the correct values.
   */
  private ServiceAccount sanitizeAccount(final ServiceAccount serviceAccount) {
    // We don't know the ID before the resource is created
    serviceAccount.setId(null);
    // we always create service accounts
    serviceAccount.setService(true);
    return serviceAccount;
  }

  private CreateServiceAccount getResourceFromDocument(final String document) {
    try {
      final JSONAPIDocument<CreateServiceAccount> resourceDocument =
          resourceConverter.readDocument(document.getBytes(StandardCharsets.UTF_8),
              CreateServiceAccount.class);
      final CreateServiceAccount serviceAccount = resourceDocument.get();
      return serviceAccount;
    } catch (final Exception ex) {
      // Assume the JSON is unable to be parsed.
      throw new InvalidInput();
    }
  }

  private String respondWithResource(final ServiceAccount serviceAccount)
      throws DocumentSerializationException {
    final JSONAPIDocument<ServiceAccount> document = new JSONAPIDocument<ServiceAccount>(
        serviceAccount);
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
