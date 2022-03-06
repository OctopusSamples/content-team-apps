package com.octopus.serviceaccount.domain.handlers;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.exceptions.InvalidClientId;
import com.octopus.exceptions.InvalidInput;
import com.octopus.exceptions.Unauthorized;
import com.octopus.features.MicroserviceNameFeature;
import com.octopus.serviceaccount.domain.entities.ApiKey;
import com.octopus.serviceaccount.domain.entities.CreateServiceAccount;
import com.octopus.serviceaccount.domain.entities.ServiceAccount;
import com.octopus.serviceaccount.domain.utils.JsonApiResourceUtils;
import com.octopus.serviceaccount.domain.utils.OctopusLoginUtils;
import com.octopus.serviceaccount.domain.utils.ServiceAuthUtils;
import com.octopus.serviceaccount.infrastructure.clients.OctopusClient;
import io.quarkus.logging.Log;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

/**
 * Handlers take the raw input from the upstream service, like Lambda or a web server, convert the
 * inputs to POJOs, apply the security rules, create an audit trail, and then pass the requests down
 * to repositories.
 */
@ApplicationScoped
public class ServiceAccountHandler {

  @Inject
  MicroserviceNameFeature microserviceNameFeature;

  @Inject
  OctopusLoginUtils octopusLoginUtils;

  @Inject
  ServiceAuthUtils serviceAuthUtils;

  @Inject
  @Named("JsonApiServiceUtils")
  JsonApiResourceUtils<CreateServiceAccount> jsonApiServiceUtils;

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
      final String idToken)
      throws DocumentSerializationException {

    if (!serviceAuthUtils.isAuthorized(authorizationHeader, serviceAuthorizationHeader)) {
      throw new Unauthorized();
    }

    try {
      // Extract the create service account action from the HTTP body.
      final CreateServiceAccount createServiceAccount = jsonApiServiceUtils.getResourceFromDocument(
          document, CreateServiceAccount.class);

      // Remove any values that are not used with new accounts.
      final ServiceAccount serviceAccount = createServiceAccount.getServiceAccount();

      // Ensure the validity of the request.
      verifyRequest(serviceAccount);

      // extract the URL of the cloud instance the service account will be created in.
      final URI octopusServerUri = URI.create("https://" + createServiceAccount.getOctopusServer());

      // Perform a login with the id token
      final Response response = octopusLoginUtils.logIn(
          octopusServerUri,
          idToken,
          "{}",
          octopusLoginUtils.getStateHash("{}"),
          octopusLoginUtils.getNonceHash(idToken));

      // Extract the Octopus cookies from the response.
      final List<String> cookieHeaders = octopusLoginUtils.getCookies(response);

      // Join the cookies back up, ready to be sent with future requests.
      final String cookies = String.join("; ", cookieHeaders);

      // Find the csrf value
      final Optional<String> csrf = octopusLoginUtils.getCsrf(cookieHeaders);

      // Create a new service account, passing in the cookies for auth.
      final ServiceAccount newServiceAccount = createServiceAccount(
          octopusServerUri,
          serviceAccount,
          cookies,
          csrf.orElse(""));

      // Create a new API key.
      final ApiKey newApiKey = createApiKey(
          octopusServerUri,
          ApiKey
              .builder()
              .purpose("App Builder GitHub Integration")
              .build(),
          newServiceAccount.getId(),
          cookies,
          csrf.orElse(""));

      // The response to the client merges the details of the service account and its api key.
      final CreateServiceAccount combinedResponse = CreateServiceAccount.builder()
          .apiKey(newApiKey)
          .octopusServer(createServiceAccount.getOctopusServer())
          .displayName(createServiceAccount.getDisplayName())
          .username(createServiceAccount.getUsername())
          .id(newServiceAccount.getId())
          .isService(true)
          .build();

      return jsonApiServiceUtils.respondWithResource(combinedResponse);
    } catch (final ClientWebApplicationException ex) {
      Log.error(microserviceNameFeature.getMicroserviceName() + "-ExternalRequest-Failed "
          + ex.getResponse().readEntity(String.class));
      throw new InvalidInput();
    }
  }

  private ApiKey createApiKey(final URI apiUri, final ApiKey apiKey, final String userId,
      final String cookies, final String csrf) {
    final OctopusClient remoteApi = RestClientBuilder.newBuilder()
        .baseUri(apiUri)
        .build(OctopusClient.class);
    return remoteApi.createApiKey(apiKey, userId, cookies, csrf);
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
