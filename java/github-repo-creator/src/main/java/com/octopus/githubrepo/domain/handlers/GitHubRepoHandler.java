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
import java.io.IOException;
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
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTreeBuilder;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
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
  public String create(@NonNull final String document, final String authorizationHeader,
      final String serviceAuthorizationHeader, @NonNull final String githubToken)
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

      final String decryptedGithubToken = cryptoUtils.decrypt(githubToken, githubEncryption,
          githubSalt);

      // Get the existing repo, or create a new one.
      createRepo(decryptedGithubToken, createGithubRepo);

      // Create the secrets
      createSecrets(decryptedGithubToken, createGithubRepo);

      return "yay!";
    } catch (final ClientWebApplicationException ex) {
      Log.error(microserviceNameFeature.getMicroserviceName() + "-ExternalRequest-Failed "
          + ex.getResponse().readEntity(String.class));
      throw new InvalidInput();
    } catch (final Throwable ex) {
      throw new InvalidInput();
    }
  }

  private void commitFiles(final String githubToken, final CreateGithubRepo createGithubRepo) throws IOException {
    GitHub gitHub =  new GitHubBuilder().withOAuthToken(githubToken).build();

    // start with a Repository ref
    GHRepository repo = gitHub.getRepository(createGithubRepo.getGithubRepository());

    // get a sha to represent the root branch to start your commit from.
    // this can come from any number of classes:
    //   GHBranch, GHCommit, GHTree, etc...

    // for this example, we'll start from the master branch
    GHBranch masterBranch = repo.getBranch("main");

    // get a tree builder object to build up into the commit.
    // the base of the tree will be the master branch
    GHTreeBuilder treeBuilder = repo.createTree().baseTree(masterBranch.getSHA1());

    // add entries to the tree in various ways.
    // the nice thing about GHTreeBuilder.textEntry() is that it is an "upsert" (create new or update existing)
    //treeBuilder = treeBuilder.add(pathToFile, contentOfFile, false);

    // perform the commit
    GHCommit commit = repo.createCommit()
        // base the commit on the tree we built
        .tree(treeBuilder.create().getSha())
        // set the parent of the commit as the master branch
        .parent(masterBranch.getSHA1()).message("App Builder repo population").create();
  }

  private void createRepo(final String decryptedGithubToken,
      final CreateGithubRepo createGithubRepo) {
    try {
      gitHubClient.getRepo(createGithubRepo.getGithubOwner(),
          createGithubRepo.getGithubRepository(), "token " + decryptedGithubToken);
    } catch (ClientWebApplicationException ex) {
      if (ex.getResponse().getStatus() == 404) {
        gitHubClient.createRepo(
            GithubRepo.builder().name(createGithubRepo.getGithubRepository()).build(),
            "token " + decryptedGithubToken);
      } else {
        throw ex;
      }
    }
  }

  private void createSecrets(final String decryptedGithubToken,
      final CreateGithubRepo createGithubRepo) {
    // Create the sodium key.
    final GitHubPublicKey publicKey = gitHubClient.getPublicKey("token " + decryptedGithubToken,
        createGithubRepo.getGithubOwner(), createGithubRepo.getGithubRepository());

    // Add the repository secrets.
    if (createGithubRepo.getSecrets() != null) {
      for (final Secret secret : createGithubRepo.getSecrets()) {
        try (final SecretBox box = SecretBox.encrypt(
            SecretBox.key(Base64.getDecoder().decode(publicKey.getKey())), secret.getValue())) {
          gitHubClient.createSecret(
              GitHubSecret.builder().encryptedValue(box.toString()).keyId(publicKey.getKeyId())
                  .build(), "token " + decryptedGithubToken, createGithubRepo.getGithubOwner(),
              createGithubRepo.getGithubRepository(), secret.getName());
        }
      }
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
