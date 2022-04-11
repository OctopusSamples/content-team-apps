package com.octopus.githubrepo.domain.handlers;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.google.common.base.Preconditions;
import com.octopus.encryption.CryptoUtils;
import com.octopus.exceptions.InvalidInputException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.features.MicroserviceNameFeature;
import com.octopus.githubrepo.GlobalConstants;
import com.octopus.githubrepo.domain.entities.CreateGithubCommit;
import com.octopus.githubrepo.domain.entities.PopulateGithubRepo;
import com.octopus.githubrepo.domain.entities.github.GitHubUser;
import com.octopus.githubrepo.domain.entities.github.GithubRepo;
import com.octopus.githubrepo.domain.features.DisableServiceFeature;
import com.octopus.githubrepo.domain.framework.producers.JsonApiConverter;
import com.octopus.githubrepo.domain.utils.JsonApiResourceUtils;
import com.octopus.githubrepo.domain.utils.ServiceAuthUtils;
import com.octopus.githubrepo.infrastructure.clients.GitHubClient;
import com.octopus.githubrepo.infrastructure.clients.PopulateRepoClient;
import dev.failsafe.RetryPolicy;
import io.quarkus.logging.Log;
import io.vavr.control.Try;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

/**
 * Handlers take the raw input from the upstream service, like Lambda or a web server, convert the
 * inputs to POJOs, apply the security rules, create an audit trail, and then pass the requests down
 * to repositories.
 *
 * <p>The GitHub Commit handler is responsible for creating a new commit in GitHub. Every time the
 * App Builder is run, a new commit is created somewhere, so this maps nicely to the idea
 * of a REST endpoint creating a new commit with a POST request.
 *
 * <p>This handler is expected to return a 202 HTTP code to the caller to indicate that the request
 * was accepted, but that the actual commit is going to be created later. The response body
 * includes the details of the GitHub repo where the commit will be created.
 *
 * <p>This handler solves a number of issues:
 * 1. It is more RESTful than simply executing an action to populate repo. Conceptually, we are
 *    creating a new commit every time this service is run, which maps nicely to a JSONAPI
 *    create endpoint.
 * 2. It allows us to return the details of the github repo and user. The GitHub user token
 *    is encrypted and not usable by JavaScript. This is inline with the fact that the GitHub
 *    Oauth infrastructure does not support the implicit flow. But it also means there is no
 *    direct way to get the details of the user. So returning the user and repo details
 *    from this endpoint allows the frontend app to then know those minimal details.
 * 3. The long-running operation of populating the repo is handled async and API clients don't
 *    need to be aware of the details.
 *
 * <p>It is the responsibility of the GitHubRepoHandler to create the commit with the contents of the
 * template to be saved in the git repo. This can take a bit of time, and so is done async.
 */
@ApplicationScoped
public class GitHubCommitHandler {

  /**
   * The default branch.
   */
  private static final String DEFAULT_BRANCH = "main";

  /**
   * The branch we place any subsequent app builder deployments into. Doing so ensures we don't
   * overwrite any updates users may have made between running the app-builder. The workflows are
   * also configured to not run on this branch, so any manual updates made to Octopus won't be
   * reverted.
   */
  private static final String UPDATE_BRANCH = "app-builder-update";

  @ConfigProperty(name = "github.encryption")
  String githubEncryption;

  @ConfigProperty(name = "github.salt")
  String githubSalt;

  @Inject
  MicroserviceNameFeature microserviceNameFeature;

  @RestClient
  GitHubClient gitHubClient;

  @RestClient
  PopulateRepoClient populateRepoClient;

  @Inject
  ServiceAuthUtils serviceAuthUtils;

  @Inject
  @Named("JsonApiServiceUtilsCreateGithubCommit")
  JsonApiResourceUtils<CreateGithubCommit> jsonApiServiceUtilsCreateGithubCommit;

  @Inject
  @Named("JsonApiServiceUtilsCreateGithubRepo")
  JsonApiResourceUtils<PopulateGithubRepo> jsonApiServiceUtilsCreateGithubRepo;

  @Inject
  CryptoUtils cryptoUtils;

  @Inject
  Validator validator;

  @Inject
  DisableServiceFeature disableServiceFeature;

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
      final String routingHeader,
      @NonNull final String githubToken)
      throws DocumentSerializationException {

    Preconditions.checkArgument(StringUtils.isNotBlank(document), "document can not be blank");
    Preconditions.checkArgument(StringUtils.isNotBlank(githubToken),
        "githubToken can not be blank");

    if (!serviceAuthUtils.isAuthorized(authorizationHeader, serviceAuthorizationHeader)) {
      throw new UnauthorizedException();
    }

    if (disableServiceFeature.getDisableRepoCreation()) {
      return jsonApiServiceUtilsCreateGithubCommit.respondWithResource(CreateGithubCommit
          .builder()
          .id("")
          .githubRepository("")
          .build());
    }

    try {
      // Extract the create service account action from the HTTP body.
      final CreateGithubCommit createGithubRepo = jsonApiServiceUtilsCreateGithubCommit
          .getResourceFromDocument(document, CreateGithubCommit.class);

      // Ensure the validity of the request.
      verifyRequest(createGithubRepo);

      // Decrypt the github token passed in as a cookie.
      final String decryptedGithubToken = cryptoUtils.decrypt(
          githubToken,
          githubEncryption,
          githubSalt);

      // Ensure we have the required scopes
      verifyScopes(decryptedGithubToken);

      // Get the GitHub login name
      final GitHubUser user = gitHubClient.getUser("token " + decryptedGithubToken);

      // Get the destination repo name
      final String repoName = createGithubRepo.isCreateNewRepo()
          ? getUniqueRepoName(decryptedGithubToken, createGithubRepo, user)
          : createGithubRepo.getGithubRepository();

      // Create the repo
      final String branch = createRepo(decryptedGithubToken, user, repoName)
          ? DEFAULT_BRANCH : UPDATE_BRANCH;

      // Make an async call to populate the new repo
      try (final Response response = populateRepoClient.populateRepo(jsonApiServiceUtilsCreateGithubRepo.respondWithResource(
              PopulateGithubRepo
                  .builder()
                  .generator(createGithubRepo.getGenerator())
                  .githubRepository(repoName)
                  .branch(branch)
                  .options(createGithubRepo.getOptions())
                  .secrets(createGithubRepo.getSecrets())
                  .build()),
          routingHeader,
          authorizationHeader,
          null,
          GlobalConstants.ASYNC_INVOCATION_TYPE,
          githubToken)) {
        if (response.getStatus() != 202) {
          Log.error(microserviceNameFeature.getMicroserviceName() + "-PopulateRepo-UnexpectedResponse Response code was " + response.getStatus() + " but expected a 202");
        }
      }

      // return the details of the new repo
      return jsonApiServiceUtilsCreateGithubCommit.respondWithResource(CreateGithubCommit
          .builder()
          .id(user.getLogin() + "/" + repoName)
          .githubRepository(repoName)
          .githubOwner(user.getLogin())
          .githubBranch(branch)
          .build());
    } catch (final ClientWebApplicationException ex) {
      Try.run(
          () -> Log.error(microserviceNameFeature.getMicroserviceName() + "-ExternalRequest-Failed "
              + ex.getResponse().readEntity(String.class), ex));
      throw new InvalidInputException();
    } catch (final InvalidInputException | IllegalArgumentException ex) {
      Log.error(
          microserviceNameFeature.getMicroserviceName() + "-Request-Failed", ex);
      throw ex;
    } catch (final Throwable ex) {
      Log.error(microserviceNameFeature.getMicroserviceName() + "-General-Failure", ex);
      throw new InvalidInputException();
    }
  }

  /**
   * This tool won't work without certain scopes granted in the OAuth token. This method verifies
   * the appropriate scopes are available by making a request to a no-op endpoint and reading the
   * headers in the response.
   */
  private void verifyScopes(final String decryptedGithubToken) {
    try (final Response response = gitHubClient.checkRateLimit("token " + decryptedGithubToken)) {
      final List<String> scopes =
          Arrays.stream(response
                  .getHeaderString("X-OAuth-Scopes")
                  .split(","))
              .map(String::trim)
              .collect(Collectors.toList());

      if (!scopes.contains("workflow")) {
        throw new InvalidInputException("GitHub token did not have the workflow scope");
      }

      if (!scopes.contains("repo")) {
        throw new InvalidInputException("GitHub token did not have the repo scope");
      }
    }
  }

  private String getUniqueRepoName(
      final String decryptedGithubToken,
      final CreateGithubCommit createGithubRepo,
      final GitHubUser user) {
    String repoName = createGithubRepo.getGithubRepository();

    // If we want to create a fresh repo every time, add a counter to the end of the repo name
    for (int i = 1; i <= 100; ++i) {

      if (doesRepoExist(decryptedGithubToken, user, repoName)) {
        repoName = createGithubRepo.getGithubRepository() + i;
      } else {
        break;
      }
    }

    return repoName;
  }

  private boolean createRepo(final String decryptedGithubToken,
      final GitHubUser user,
      final String repoName) {

    /*
     If we are creating a unique repo name, or creating a new common repo, go ahead and
     create a new GitHub repo.
     */
    if (!doesRepoExist(decryptedGithubToken, user, repoName)) {
      gitHubClient.createRepo(
          GithubRepo.builder().name(repoName).build(),
          "token " + decryptedGithubToken);
      return true;
    }

    return false;
  }

  private boolean doesRepoExist(
      final String decryptedGithubToken,
      final GitHubUser user,
      final String repoName) {
    try {
      final Response response = gitHubClient.getRepo(
          user.getLogin(),
          repoName,
          "token " + decryptedGithubToken);

      if (response.getStatus() == 200) {
        return true;
      }
    } catch (ClientWebApplicationException ex) {
      if (ex.getResponse().getStatus() != 404) {
        /*
          Anything else was unexpected, and will result in an error.
         */
        Log.error(microserviceNameFeature.getMicroserviceName() + "-CreateRepo-FindRepoError", ex);
        throw ex;
      }
    }

    return false;
  }

  /**
   * Ensure the service account being created has the correct values.
   */
  private void verifyRequest(final CreateGithubCommit resource) {
    final Set<ConstraintViolation<CreateGithubCommit>> violations = validator.validate(resource);
    if (violations.isEmpty()) {
      return;
    }

    throw new InvalidInputException(
        violations.stream().map(cv -> cv.getMessage()).collect(Collectors.joining(", ")));
  }
}
