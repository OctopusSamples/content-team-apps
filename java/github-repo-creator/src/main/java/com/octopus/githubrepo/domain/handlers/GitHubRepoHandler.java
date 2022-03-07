package com.octopus.githubrepo.domain.handlers;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.encryption.CryptoUtils;
import com.octopus.exceptions.InvalidInput;
import com.octopus.exceptions.Unauthorized;
import com.octopus.features.MicroserviceNameFeature;
import com.octopus.githubrepo.domain.entities.CreateGithubRepo;
import com.octopus.githubrepo.domain.entities.GitHubPublicKey;
import com.octopus.githubrepo.domain.entities.GitHubSecret;
import com.octopus.githubrepo.domain.entities.GithubRepo;
import com.octopus.githubrepo.domain.entities.Secret;
import com.octopus.githubrepo.domain.utils.JsonApiResourceUtils;
import com.octopus.githubrepo.domain.utils.ServiceAuthUtils;
import com.octopus.githubrepo.infrastructure.clients.GitHubClient;
import io.quarkus.logging.Log;
import io.vavr.control.Try;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import software.pando.crypto.nacl.SecretBox;

/**
 * Handlers take the raw input from the upstream service, like Lambda or a web server, convert the
 * inputs to POJOs, apply the security rules, create an audit trail, and then pass the requests down
 * to repositories.
 */
@ApplicationScoped
public class GitHubRepoHandler {

  @ConfigProperty(name = "github.encryption")
  String githubEncryption;

  @ConfigProperty(name = "github.salt")
  String githubSalt;

  @Inject
  MicroserviceNameFeature microserviceNameFeature;

  @RestClient
  GitHubClient gitHubClient;

  @Inject
  ServiceAuthUtils serviceAuthUtils;

  @Inject
  @Named("JsonApiServiceUtils")
  JsonApiResourceUtils<CreateGithubRepo> jsonApiServiceUtils;

  @Inject
  CryptoUtils cryptoUtils;

  @Inject
  Validator validator;

  /**
   * Creates a new service account in the Octopus cloud instance.
   *
   * @param document                   The JSONAPI resource to create.
   * @param authorizationHeader        The OAuth header for user-to-machine communication from the
   *                                   content team identity management system. Note this is not
   *                                   Octofront, but probably Cognito.
   * @param serviceAuthorizationHeader The OAuth header for machine-to-machine communication. Note
   *                                   this is not Octofront, but probably Cognito.
   * @return The newly created resource
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI
   *                                        resource.
   */
  public String create(
      @NonNull final String document,
      final String authorizationHeader,
      final String serviceAuthorizationHeader,
      @NonNull final String githubToken)
      throws DocumentSerializationException {

    if (!serviceAuthUtils.isAuthorized(authorizationHeader, serviceAuthorizationHeader)) {
      throw new Unauthorized();
    }

    try {
      // Extract the create service account action from the HTTP body.
      final CreateGithubRepo createGithubRepo = jsonApiServiceUtils.getResourceFromDocument(
          document, CreateGithubRepo.class);

      // Ensure the validity of the request.
      verifyRequest(createGithubRepo);

      final String decryptedGithubToken = cryptoUtils.decrypt(
          githubToken,
          githubEncryption,
          githubSalt);

      // Get the existing repo, or create a new one.
      Try.of(() -> gitHubClient.getRepo(
          createGithubRepo.getGithubOwner(),
          createGithubRepo.getGithubRepository(),
          "token " + decryptedGithubToken
      )).recover(ClientWebApplicationException.class, e -> {
        // If the repo does not exist, create it.
        if (e.getResponse().getStatus() == 404) {
          return gitHubClient.createRepo(
              GithubRepo.builder().name(createGithubRepo.getGithubRepository()).build(),
              "token " + decryptedGithubToken);
        }
        throw e;
      }).getOrElseThrow(e -> e);

      // Create the sodium key.
      final GitHubPublicKey publicKey = gitHubClient.getPublicKey(
          "token " + decryptedGithubToken,
          createGithubRepo.getGithubOwner(),
          createGithubRepo.getGithubRepository()
      );

      // Add the repository secrets.
      if (createGithubRepo.getSecrets() != null) {
        for (final Secret secret : createGithubRepo.getSecrets()) {
          try (final SecretBox box = SecretBox.encrypt(
            SecretBox.key(Base64.getDecoder().decode(publicKey.getKey())),
            secret.getValue())) {
            final Response createSecretResponse = gitHubClient.createSecret(
                GitHubSecret.builder()
                    .encryptedValue(box.toString())
                    .keyId(publicKey.getKeyId())
                    .build(),
                "token " + decryptedGithubToken,
                createGithubRepo.getGithubOwner(),
                createGithubRepo.getGithubRepository(),
                secret.getName()
            );
          }
        }
      }

      return "yay!";
    } catch (final ClientWebApplicationException ex) {
      Log.error(microserviceNameFeature.getMicroserviceName() + "-ExternalRequest-Failed "
          + ex.getResponse().readEntity(String.class));
      throw new InvalidInput();
    } catch (final Throwable ex) {
      throw new InvalidInput();
    }
  }

  /**
   * Ensure the service account being created has the correct values.
   */
  private void verifyRequest(final CreateGithubRepo resource) {
    final Set<ConstraintViolation<CreateGithubRepo>> violations = validator.validate(resource);
    if (violations.isEmpty()) {
      return;
    }

    throw new InvalidInput(
        violations.stream().map(cv -> cv.getMessage()).collect(Collectors.joining(", ")));
  }
}
