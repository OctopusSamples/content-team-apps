package com.octopus.githubrepo.domain.handlers;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.encryption.CryptoUtils;
import com.octopus.exceptions.InvalidClientId;
import com.octopus.exceptions.InvalidInput;
import com.octopus.exceptions.Unauthorized;
import com.octopus.features.MicroserviceNameFeature;
import com.octopus.githubrepo.domain.entities.ApiKey;
import com.octopus.githubrepo.domain.entities.CreateServiceAccount;
import com.octopus.githubrepo.domain.entities.OctopusApiKey;
import com.octopus.githubrepo.domain.entities.ServiceAccount;
import com.octopus.githubrepo.domain.entities.User;
import com.octopus.githubrepo.domain.entities.Users;
import com.octopus.githubrepo.domain.features.DisableAccountCreationFeature;
import com.octopus.githubrepo.domain.utils.JsonApiResourceUtils;
import com.octopus.githubrepo.domain.utils.OctopusLoginUtils;
import com.octopus.githubrepo.domain.utils.ServiceAuthUtils;
import com.octopus.githubrepo.infrastructure.clients.OctopusClient;
import io.quarkus.logging.Log;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

/**
 * Handlers take the raw input from the upstream service, like Lambda or a web server, convert the
 * inputs to POJOs, apply the security rules, create an audit trail, and then pass the requests down
 * to repositories.
 */
@ApplicationScoped
public class ServiceAccountHandler {

  private static final String API_KEY_DESCRIPTION = "App Builder GitHub Integration";

  @ConfigProperty(name = "octopus.encryption")
  String octopusEncryption;

  @ConfigProperty(name = "octopus.salt")
  String octopusSalt;

  @Inject
  MicroserviceNameFeature microserviceNameFeature;

  @Inject
  OctopusLoginUtils octopusLoginUtils;

  @Inject
  ServiceAuthUtils serviceAuthUtils;

  @Inject
  @Named("JsonApiServiceUtils")
  JsonApiResourceUtils<CreateServiceAccount> jsonApiServiceUtils;

  @Inject
  DisableAccountCreationFeature disableAccountCreationFeature;

  @Inject
  CryptoUtils cryptoUtils;

  /**
   * Creates a new service account in the Octopus cloud instance.
   *
   * @param document                   The JSONAPI resource to create.
   * @param authorizationHeader        The OAuth header for user-to-machine communication from the
   *                                   content team identity management system. Note this is not
   *                                   Octofront, but probably Cognito.
   * @param serviceAuthorizationHeader The OAuth header for machine-to-machine communication. Note
   *                                   this is not Octofront, but probably Cognito.
   * @param idToken                    The ID Token from Octofront representing the end user's login
   *                                   to OctoID.
   * @return The newly created resource
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *                                        resource.
   */
  public String create(
      @NonNull final String document,
      final String authorizationHeader,
      final String serviceAuthorizationHeader,
      @NonNull final String idToken)
      throws DocumentSerializationException {

    if (!serviceAuthUtils.isAuthorized(authorizationHeader, serviceAuthorizationHeader)) {
      throw new Unauthorized();
    }

    if (disableAccountCreationFeature.getDisableAccountCreation()) {
      return getEmptyResponse();
    }

    try {
      // Extract the create service account action from the HTTP body.
      final CreateServiceAccount createServiceAccount = jsonApiServiceUtils.getResourceFromDocument(
          document, CreateServiceAccount.class);

      // Remove any values that are not used with new accounts.
      final ServiceAccount serviceAccount = createServiceAccount.convertToServiceAccount();

      // Ensure the validity of the request.
      verifyRequest(serviceAccount);

      // extract the URL of the cloud instance the service account will be created in.
      final URI octopusServerUri = URI.create("https://" + createServiceAccount.getOctopusServer());

      final String decryptedIdToken = cryptoUtils.decrypt(
          idToken,
          octopusEncryption,
          octopusSalt);

      // Perform a login with the id token
      final Response response = octopusLoginUtils.logIn(
          octopusServerUri,
          decryptedIdToken,
          "{}",
          octopusLoginUtils.getStateHash("{}"),
          octopusLoginUtils.getNonceHash(decryptedIdToken));

      // Extract the Octopus cookies from the response.
      final List<String> cookieHeaders = octopusLoginUtils.getCookies(response);

      // Join the cookies back up, ready to be sent with future requests.
      final String cookies = String.join("; ", cookieHeaders);

      // Find the csrf value
      final Optional<String> csrf = octopusLoginUtils.getCsrf(cookieHeaders);

      // Get the existing account, or create a new one.
      final String accountId = getExistingAccount(
          octopusServerUri,
          cookies,
          csrf.orElse(""),
          serviceAccount.getUsername())
          .orElseGet(() -> createServiceAccount(
              octopusServerUri,
              serviceAccount,
              cookies,
              csrf.orElse("")).getId());

      // Create a new API key against the service account.
      final OctopusApiKey newApiKey = createApiKey(
          octopusServerUri,
          ApiKey
              .builder()
              .purpose(API_KEY_DESCRIPTION)
              .build(),
          accountId,
          cookies,
          csrf.orElse(""));

      // The response to the client merges the details of the service account and its api key.
      final CreateServiceAccount combinedResponse = CreateServiceAccount.builder()
          .apiKey(ApiKey.builder()
              .id(newApiKey.getId())
              .apiKey(newApiKey.getApiKey())
              .build())
          .octopusServer(createServiceAccount.getOctopusServer())
          .displayName(createServiceAccount.getDisplayName())
          .username(createServiceAccount.getUsername())
          .id(accountId)
          .isService(true)
          .build();

      return jsonApiServiceUtils.respondWithResource(combinedResponse);
    } catch (final ClientWebApplicationException ex) {
      Log.error(microserviceNameFeature.getMicroserviceName() + "-ExternalRequest-Failed "
          + ex.getResponse().readEntity(String.class));
      throw new InvalidInput();
    }
  }

  private String getEmptyResponse() throws DocumentSerializationException {
    final CreateServiceAccount combinedResponse = CreateServiceAccount.builder()
        .apiKey(ApiKey.builder()
            .id("")
            .apiKey("")
            .build())
        .octopusServer("")
        .displayName("")
        .username("")
        .id("")
        .isService(false)
        .build();

    return jsonApiServiceUtils.respondWithResource(combinedResponse);
  }

  private OctopusApiKey createApiKey(final URI apiUri, final ApiKey apiKey, final String userId,
      final String cookies, final String csrf) {
    final OctopusClient remoteApi = RestClientBuilder.newBuilder()
        .baseUri(apiUri)
        .build(OctopusClient.class);
    return remoteApi.createApiKey(apiKey, userId, cookies, csrf);
  }

  private Optional<String> getExistingAccount(final URI apiUri, final String cookies,
      final String csrf, final String username) {
    final OctopusClient remoteApi = RestClientBuilder.newBuilder()
        .baseUri(apiUri)
        .build(OctopusClient.class);
    final Users users = remoteApi.getUsers(cookies, csrf, username);
    return users.getItems().stream()
        .filter(u -> username.equals(u.getUsername()))
        .map(User::getId)
        .findFirst();
  }

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
  private void verifyRequest(final ServiceAccount serviceAccount) {
    // The client must not specify an ID
    if (serviceAccount.getId() != null) {
      throw new InvalidClientId();
    }

    if (!serviceAccount.isService()) {
      throw new InvalidInput("The service attribute must be true");
    }
  }
}
