package com.octopus.githubrepo.domain.handlers;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.google.common.base.Preconditions;
import com.goterl.lazysodium.LazySodium;
import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import com.goterl.lazysodium.exceptions.SodiumException;
import com.goterl.lazysodium.utils.Base64MessageEncoder;
import com.goterl.lazysodium.utils.Key;
import com.goterl.lazysodium.utils.LibraryLoader.Mode;
import com.octopus.encryption.AsymmetricDecryptor;
import com.octopus.encryption.CryptoUtils;
import com.octopus.exceptions.InvalidInputException;
import com.octopus.exceptions.ServerErrorException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.features.MicroserviceNameFeature;
import com.octopus.files.TemporaryResources;
import com.octopus.githubrepo.domain.entities.GenerateTemplate;
import com.octopus.githubrepo.domain.entities.PopulateGithubRepo;
import com.octopus.githubrepo.domain.entities.Secret;
import com.octopus.githubrepo.domain.entities.github.GitHubCommit;
import com.octopus.githubrepo.domain.entities.github.GitHubPublicKey;
import com.octopus.githubrepo.domain.entities.github.GitHubSecret;
import com.octopus.githubrepo.domain.entities.github.GitHubUser;
import com.octopus.githubrepo.domain.entities.github.GithubFile;
import com.octopus.githubrepo.domain.entities.github.GithubRef;
import com.octopus.githubrepo.domain.entities.github.GithubRepo;
import com.octopus.githubrepo.domain.exceptions.GitHubException;
import com.octopus.githubrepo.domain.features.DisableServiceFeature;
import com.octopus.githubrepo.domain.framework.producers.JsonApiConverter;
import com.octopus.githubrepo.domain.utils.JsonApiResourceUtils;
import com.octopus.githubrepo.domain.utils.LinksHeaderParsing;
import com.octopus.githubrepo.domain.utils.ScopeVerifier;
import com.octopus.githubrepo.domain.utils.ServiceAuthUtils;
import com.octopus.githubrepo.infrastructure.clients.GenerateTemplateClient;
import com.octopus.githubrepo.infrastructure.clients.GitHubClient;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import io.quarkus.logging.Log;
import io.vavr.control.Try;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTreeBuilder;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.internal.DefaultGitHubConnector;

/**
 * Handlers take the raw input from the upstream service, like Lambda or a web server, convert the inputs to POJOs, apply the security rules, create an audit
 * trail, and then pass the requests down to repositories.
 */
@ApplicationScoped
public class GitHubRepoHandler {

  /**
   * The default branch.
   */
  private static final String DEFAULT_BRANCH = "main";

  /**
   * The branch we place any subsequent app builder deployments into. Doing so ensures we don't overwrite any updates users may have made between running the
   * app-builder. The workflows are also configured to not run on this branch, so any manual updates made to Octopus won't be reverted.
   */
  private static final String UPDATE_BRANCH = "app-builder-update";

  /**
   * A list of directories we know we don't want to commit to a new repo.
   */
  private static final String[] IGNORE_PATHS = {".git", "target", "node_modules"};

  /**
   * A retry policy used when calling upstream services.
   */
  private static final RetryPolicy<String> RETRY_POLICY = RetryPolicy
      .<String>builder()
      .handle(Exception.class)
      .withDelay(Duration.ofSeconds(3))
      .withMaxRetries(3)
      .build();

  @ConfigProperty(name = "github.encryption")
  String githubEncryption;

  @ConfigProperty(name = "github.salt")
  String githubSalt;

  @ConfigProperty(name = "client.private-key-base64")
  String privateKeyBase64;

  @Inject
  MicroserviceNameFeature microserviceNameFeature;

  @RestClient
  GitHubClient gitHubClient;

  @RestClient
  GenerateTemplateClient generateTemplateClient;

  @Inject
  ServiceAuthUtils serviceAuthUtils;

  @Inject
  @Named("JsonApiServiceUtilsCreateGithubRepo")
  JsonApiResourceUtils<PopulateGithubRepo> jsonApiServiceUtilsCreateGithubRepo;

  @Inject
  CryptoUtils cryptoUtils;

  @Inject
  AsymmetricDecryptor asymmetricDecryptor;

  @Inject
  Validator validator;

  @Inject
  JsonApiConverter jsonApiConverter;

  @Inject
  DisableServiceFeature disableServiceFeature;

  @Inject
  LinksHeaderParsing linksHeaderParsing;

  @Inject
  GitHubBuilder gitHubBuilder;

  @Inject
  ScopeVerifier scopeVerifier;

  /**
   * Creates a new service account in the Octopus cloud instance.
   *
   * @param document                   The JSONAPI resource to create.
   * @param authorizationHeader        The OAuth header for user-to-machine communication from the content team identity management system. Note this is not
   *                                   Octofront, but probably Cognito.
   * @param serviceAuthorizationHeader The OAuth header for machine-to-machine communication. Note this is not Octofront, but probably Cognito.
   * @return The newly created resource
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI resource.
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
      return jsonApiServiceUtilsCreateGithubRepo.respondWithResource(PopulateGithubRepo
          .builder()
          .id("")
          .githubRepository("")
          .build());
    }

    try {
      // Extract the create service account action from the HTTP body.
      final PopulateGithubRepo populateGithubRepo = jsonApiServiceUtilsCreateGithubRepo.getResourceFromDocument(
          document, PopulateGithubRepo.class);

      // Ensure the validity of the request.
      verifyRequest(populateGithubRepo);

      // Decrypt the github token passed in as a cookie.
      final String decryptedGithubToken = cryptoUtils.decrypt(githubToken, githubEncryption,
          githubSalt);

      // Ensure we have the required scopes
      scopeVerifier.verifyScopes(decryptedGithubToken);

      // Get the GitHub login name
      final GitHubUser user = gitHubClient.getUser("token " + decryptedGithubToken);

      /*
       Ensure the repo exists. We do expect that the repo was created by an upstream service
       before we get here, but we also support creating the repo as part of this request.
       */
      Failsafe.with(RETRY_POLICY)
          .run(() -> createRepo(decryptedGithubToken, user, populateGithubRepo.getGithubRepository()));

      // Create the secrets
      Failsafe.with(RETRY_POLICY)
          .run(() -> createSecrets(decryptedGithubToken, populateGithubRepo, user,
          populateGithubRepo.getGithubRepository()));

      // Ensure there is an initial commit in the repo to use as the basis of other commits
      Failsafe.with(RETRY_POLICY)
          .run(() -> populateInitialFile(decryptedGithubToken, populateGithubRepo, user,
          populateGithubRepo.getGithubRepository()));

      /*
        Ensure the branch exists.
      */
      Failsafe.with(RETRY_POLICY)
          .run(() -> createBranch(decryptedGithubToken, user, populateGithubRepo.getGithubRepository(), populateGithubRepo.getBranch()));

      // Download and extract the template zip file
      final String templateDir = Failsafe.with(RETRY_POLICY)
          .get(() -> downloadTemplate(
              populateGithubRepo,
              authorizationHeader,
              null,
              routingHeader));

      // Commit the files
      final String commit = Failsafe.with(RETRY_POLICY)
          .get(() -> commitFiles(
              decryptedGithubToken,
              user,
              populateGithubRepo.getGithubRepository(),
              templateDir,
              populateGithubRepo.getBranch()));

      // return the details of the new commit
      return jsonApiServiceUtilsCreateGithubRepo.respondWithResource(PopulateGithubRepo
          .builder()
          .id(user.getLogin() + "/" + populateGithubRepo.getGithubRepository() + "/commits/"
              + commit)
          .githubRepository(populateGithubRepo.getGithubRepository())
          .build());
    } catch (final ClientWebApplicationException ex) {
      Try.run(
          () -> Log.error(microserviceNameFeature.getMicroserviceName() + "-ExternalRequest-Failed "
              + ex.getResponse().readEntity(String.class), ex));
      throw new ServerErrorException();
    } catch (final InvalidInputException | IllegalArgumentException ex) {
      Log.error(
          microserviceNameFeature.getMicroserviceName() + "-Request-Failed", ex);
      throw ex;
    } catch (final Throwable ex) {
      Log.error(microserviceNameFeature.getMicroserviceName() + "-General-Failure", ex);
      throw new ServerErrorException();
    }
  }

  private String downloadTemplate(
      final PopulateGithubRepo populateGithubRepo,
      final String authHeader,
      final String serviceAuthHeader,
      final String routingHeader)
      throws DocumentSerializationException, IOException {

    final GenerateTemplate generateTemplate = GenerateTemplate.builder()
        .id("")
        .generator(populateGithubRepo.getGenerator())
        .options(populateGithubRepo.getOptions())
        .build();

    final String body = new String(jsonApiConverter.buildResourceConverter().writeDocument(
        new JSONAPIDocument<>(generateTemplate)));

    try (final TemporaryResources temp = new TemporaryResources()) {
      final Path zipFile = downloadTemplateToTempFile(
          body, temp, authHeader, serviceAuthHeader, routingHeader);
      return extractZipToTempDir(zipFile, temp).toString();
    }
  }

  private Path downloadTemplateToTempFile(
      final String body,
      final TemporaryResources temp,
      final String authHeader,
      final String serviceAuthHeader,
      final String routingHeader)
      throws IOException {

    Log.info("Calling template generator");

    try (final Response response = generateTemplateClient.generateTemplate(
        body, routingHeader, authHeader, serviceAuthHeader)) {
      if (response.getStatus() != 200) {
        throw new BadRequestException(
            "Call to template generator resulted in status code " + response.getStatus());
      }

      final InputStream inputStream = response.readEntity(InputStream.class);
      final Path targetFile = temp.createTempFile("template", ".zip");
      FileUtils.copyInputStreamToFile(inputStream, targetFile.toFile());
      return targetFile;
    }
  }

  private Path extractZipToTempDir(final Path targetFile, final TemporaryResources temp)
      throws IOException {
    try (final ZipFile zip = new ZipFile(targetFile.toString())) {
      final Path destination = temp.createTempDirectory("template");
      zip.extractAll(destination.toString());
      return destination;
    } catch (final ZipException ex) {
      /*
        If we failed to extract the file it is likely that the call to the template generator
        service failed. The failure will be in the file that was saved, so we log that.
       */
      Log.error(microserviceNameFeature.getMicroserviceName() + "-Template-ExtractFailed", ex);
      Log.error(FileUtils.readFileToString(targetFile.toFile(), StandardCharsets.UTF_8));
      throw ex;
    }
  }

  private String commitFiles(
      final String githubToken,
      final GitHubUser user,
      final String repoName,
      final String path,
      final String branch) throws IOException {
    final Path inputPath = Paths.get(path);

    final GitHub gitHub = gitHubBuilder
        .withOAuthToken(githubToken)
        /*
          Note that we must define a connector here, as the GitHubConnectorSubstitution removed
          the default value from native builds to fix a compilation error.
         */
        .withConnector(DefaultGitHubConnector.create())
        .build();

    // start with a Repository ref
    final GHRepository repo = gitHub.getRepository(
        user.getLogin() + "/" + repoName);

    // get a sha to represent the root branch to start your commit from.
    // this can come from any number of classes:
    //   GHBranch, GHCommit, GHTree, etc...

    // for this example, we'll start from the main branch
    final GHBranch targetBranch = repo.getBranch(branch);

    // get a tree builder object to build up into the commit.
    // the base of the tree will be the master branch
    GHTreeBuilder treeBuilder = repo.createTree().baseTree(targetBranch.getSHA1());

    // loop over all the files and add them to the treebuilder.
    if (Files.exists(inputPath)) {
      Collection<File> files = FileUtils.listFiles(new File(path), null, true);
      for (final File file : files) {

        // don't process git dirs if they exist
        if (!pathHasIgnoreDirectory(file.getAbsolutePath())) {
          final String relativePath = inputPath.relativize(file.toPath()).toString()
              .replaceAll("\\\\", "/");

          Log.info("Adding " + relativePath);

          treeBuilder = treeBuilder.add(
              relativePath,
              com.google.common.io.Files.toByteArray(file),
              fileIsExecutable(file));
        }
      }
    }

    /*
      Perform the commit. Note that if you get a 404 here uploading a workflow file, the github token
      may not have the "workflow" scope. See
      https://stackoverflow.com/questions/68064458/how-to-upload-content-via-github-api-to-hidden-folder-name.
    */
    final GHCommit commit = repo.createCommit()
        // base the commit on the tree we built
        .tree(treeBuilder.create().getSha())
        // set the parent of the commit as the master branch
        .parent(targetBranch.getSHA1())
        .message("App Builder repo population")
        .create();

    repo.getRef("heads/" + branch).updateTo(commit.getSHA1());

    return commit.getSHA1();
  }

  private boolean fileIsExecutable(final File file) {
    return "mvnw".equals(file.getName());
  }

  private boolean pathHasIgnoreDirectory(final String path) {
    return StreamSupport.stream(Paths.get(path).spliterator(), false)
        .map(Path::toString)
        .anyMatch(p -> ArrayUtils.indexOf(IGNORE_PATHS, p) != -1);
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
   * We need to create an initial file in the repo to then allow the Git client to upload the rest.
   */
  private void populateInitialFile(
      final String decryptedGithubToken,
      final PopulateGithubRepo populateGithubRepo,
      final GitHubUser user,
      final String repoName) {

    try {
      gitHubClient.getFile(
          "token " + decryptedGithubToken,
          user.getLogin(),
          repoName,
          "README.md",
          DEFAULT_BRANCH);
    } catch (ClientWebApplicationException ex) {
      // Anything other than a 404 is unexpected
      if (ex.getResponse().getStatus() != 404) {
        throw ex;
      }

      gitHubClient.createFile(
          GithubFile.builder()
              .content(Base64.getEncoder().encodeToString(
                  ("# App Builder\n"
                      + "This repo was populated by the [Octopus Octopus Builder](https://github.com/OctopusSamples/content-team-apps) tool. The directory structure is shown below:\n\n"
                      + "* `.github/workflows`: GitHub Action Workflows that populate a cloud Octopus instance, build and deploy the sample code, and initiate a deployment in Octopus.\n"
                      + "* `github`: Composable GitHub Actions that are called by the workflow files.\n"
                      + "* `terraform`: Terraform templates used to create cloud resources and populate the Octopus cloud instance.\n"
                      + "* `java`: The sample Java application.\n"
                      + "* `js`: The sample JavaScript application.\n"
                      + "## Network Diagram\n"
                      + "![Network Diagram](/images/diagram.png)\n"
                      + "## Rerunning Octopus Builder\n"
                      + "If you have run the Octopus Builder for a second time, the files are placed in the `app-builder-update` branch.\n"
                      + "The workflow files are configured to not run from this branch, meaning any changes you have made in the main branch will not be overwritten.\n"
                      + "To replace the `main` branch with the `app-builder-update` branch, [run the following commands](https://stackoverflow.com/a/2862938/157605):\n"
                      + "1. `git checkout app-builder-update`\n"
                      + "2. `git merge -s ours main`\n"
                      + "3. `git checkout main`\n"
                      + "4. `git merge app-builder-update`\n\n"
                      + "If you would rather see what has changed since you last ran the Octopus Builder, create a regular pull request between the `app-builder-update` and `main` branches.")
                      .getBytes(StandardCharsets.UTF_8)))
              .message("Adding the initial marker file")
              .branch(DEFAULT_BRANCH)
              .build(),
          "token " + decryptedGithubToken,
          user.getLogin(),
          repoName,
          "README.md"
      );
    }
  }

  private Optional<String> getFirstSha(final String decryptedGithubToken,
      final GitHubUser user,
      final String repoName) {

    /*
     First step is to do a HTTP call where we get the raw response back. This allows us to get
     access to the response headers.
     */
    final Response response = gitHubClient.getCommitsRaw(
        user.getLogin(),
        repoName,
        1,
        "token " + decryptedGithubToken
    );

    // One of those headers may be Links, which will allow us to jump to the last page of results.
    final String linkHeader = response.getHeaderString("Link");

    // get the last page from the header
    if (StringUtils.isNotBlank(linkHeader)) {
      final Optional<String> lastPage = linksHeaderParsing.getLastPage(linkHeader);

      Log.info("Getting last SHA from page " + lastPage.orElse("1"));

      // Get the very first commit, which is actually the very last result from the list of commits.
      final List<GitHubCommit> commits = gitHubClient.getCommits(user.getLogin(),
          repoName,
          1,
          Integer.parseInt(lastPage.orElse("1")),
          "token " + decryptedGithubToken);

      // Return the first commit SHA.
      if (!commits.isEmpty()) {
        return Optional.of(commits.get(0).getSha());
      }
    }

    // If there is no Links header, just get the first commit
    final List<GitHubCommit> commits = gitHubClient.getCommits(user.getLogin(),
        repoName,
        1,
        1,
        "token " + decryptedGithubToken);

    if (!commits.isEmpty()) {
      return Optional.of(commits.get(0).getSha());
    }

    // We couldn't find the first SHA.
    return Optional.empty();
  }

  private void createBranch(
      final String decryptedGithubToken,
      final GitHubUser user,
      final String repoName,
      final String branch) {
    try {
      Log.info("Searching for branch " + branch + " under " + user.getLogin() + " " + repoName);

      // First check to see if we have the branch already.
      gitHubClient.getBranch(
          user.getLogin(),
          repoName,
          branch,
          "token " + decryptedGithubToken);

      Log.info("Found branch " + branch);
    } catch (ClientWebApplicationException ex) {
      // anything other than a 404 is unexpected
      if (ex.getResponse().getStatus() != 404) {
        throw ex;
      }

      /*
        Get the first sha for the default branch. Our new branches assume they forked the
        main repo from day one. This allows the end user to distinguish their changes
        from ours.
       */
      final Optional<String> sha = getFirstSha(decryptedGithubToken, user, repoName);

      if (sha.isPresent()) {
        Log.info("Creating branch " + branch);

        // create a new branch based on the first commit
        gitHubClient.createBranch(
            GithubRef.builder()
                .ref("refs/heads/" + branch)
                .sha(sha.get())
                .build(),
            user.getLogin(),
            repoName,
            "token " + decryptedGithubToken);
      } else {
        // We shouldn't get here, but you never know...
        Log.error(microserviceNameFeature.getMicroserviceName()
            + "-GitHub-GetShaFailed Failed to locate the first SHA from the default branch.");
        throw new GitHubException("Failed to find the first SHA");
      }
    }
  }

  private void createSecrets(
      final String decryptedGithubToken,
      final PopulateGithubRepo populateGithubRepo,
      final GitHubUser user,
      final String repoName) {

    // Create the github public key.
    final GitHubPublicKey publicKey = gitHubClient.getPublicKey(
        "token " + decryptedGithubToken,
        user.getLogin(),
        repoName);

    // Create a sodium instance
    final LazySodium lazySodium = new LazySodiumJava(
        new SodiumJava(Mode.BUNDLED_ONLY),
        new Base64MessageEncoder());

    // Create the sodium public key
    final Key githubKey = Key.fromBase64String(publicKey.getKey());

    // Add the repository secrets.
    if (populateGithubRepo.getSecrets() != null) {
      for (final Secret secret : populateGithubRepo.getSecrets()) {
        if (StringUtils.isBlank(secret.getName())) {
          continue;
        }

        if (!needToCreateSecret(secret, user, decryptedGithubToken, repoName)) {
          Log.info("Skipping existing secret " + secret.getName());
          continue;
        }

        final String secretValue = secret.isEncrypted()
            ? asymmetricDecryptor.decrypt(secret.getValue(), privateKeyBase64)
            : StringUtils.defaultString(secret.getValue(), "");

        if (StringUtils.isBlank(secretValue)) {
          Log.warn(secret.getName() + " has an blank value");
        }

        try {
          final String base64EncryptedSecret = lazySodium.cryptoBoxSealEasy(secretValue, githubKey);

          // send the encrypted value
          gitHubClient.createSecret(
              GitHubSecret.builder()
                  .encryptedValue(base64EncryptedSecret)
                  .keyId(publicKey.getKeyId())
                  .build(),
              "token " + decryptedGithubToken,
              user.getLogin(),
              repoName,
              secret.getName());
        } catch (final SodiumException ex) {
          Log.error(microserviceNameFeature.getMicroserviceName() + "-CreateSecret-GeneralError",
              ex);
          throw new RuntimeException(ex);
        }
      }
    }
  }

  private boolean needToCreateSecret(
      final Secret secret,
      final GitHubUser user,
      final String decryptedGithubToken, final String repoName) {
    // Some secrets we don't update. Check for the existing secret, and if it exists, move on.
    if (secret.isPreserveExistingSecret()) {
      return Try.of(() -> gitHubClient.getSecret(
              "token " + decryptedGithubToken,
              user.getLogin(),
              repoName,
              secret.getName()))
          .map(r -> r.getStatus() != 200)
          .getOrElse(true);
    }

    return true;
  }

  /**
   * Ensure the service account being created has the correct values.
   */
  private void verifyRequest(final PopulateGithubRepo resource) {
    final Set<ConstraintViolation<PopulateGithubRepo>> violations = validator.validate(resource);
    if (violations.isEmpty()) {
      return;
    }

    throw new InvalidInputException(
        violations.stream().map(cv -> cv.getMessage()).collect(Collectors.joining(", ")));
  }
}
