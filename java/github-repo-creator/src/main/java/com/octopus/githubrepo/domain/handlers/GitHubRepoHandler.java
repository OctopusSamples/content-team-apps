package com.octopus.githubrepo.domain.handlers;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.encryption.AsymmetricDecryptor;
import com.octopus.encryption.CryptoUtils;
import com.octopus.exceptions.InvalidInputException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.features.MicroserviceNameFeature;
import com.octopus.files.TemporaryResources;
import com.octopus.githubrepo.domain.entities.CreateGithubRepo;
import com.octopus.githubrepo.domain.entities.GenerateTemplate;
import com.octopus.githubrepo.domain.entities.GitHubPublicKey;
import com.octopus.githubrepo.domain.entities.GitHubSecret;
import com.octopus.githubrepo.domain.entities.GithubFile;
import com.octopus.githubrepo.domain.entities.GithubRepo;
import com.octopus.githubrepo.domain.entities.Secret;
import com.octopus.githubrepo.domain.features.DisableServiceFeature;
import com.octopus.githubrepo.domain.framework.producers.JsonApiConverter;
import com.octopus.githubrepo.domain.utils.JsonApiResourceUtils;
import com.octopus.githubrepo.domain.utils.ServiceAuthUtils;
import com.octopus.githubrepo.infrastructure.clients.GenerateTemplateClient;
import com.octopus.githubrepo.infrastructure.clients.GitHubClient;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import io.quarkus.logging.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
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
import software.pando.crypto.nacl.SecretBox;

/**
 * Handlers take the raw input from the upstream service, like Lambda or a web server, convert the
 * inputs to POJOs, apply the security rules, create an audit trail, and then pass the requests down
 * to repositories.
 */
@ApplicationScoped
public class GitHubRepoHandler {

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
      .withDelay(Duration.ofSeconds(1))
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
  JsonApiResourceUtils<CreateGithubRepo> jsonApiServiceUtilsCreateGithubRepo;

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
      throw new UnauthorizedException();
    }

    if (disableServiceFeature.getDisableRepoCreation()) {
      return jsonApiServiceUtilsCreateGithubRepo.respondWithResource(CreateGithubRepo
          .builder()
          .id("")
          .githubOwner("")
          .githubRepository("")
          .build());
    }

    try {
      // Extract the create service account action from the HTTP body.
      final CreateGithubRepo createGithubRepo = jsonApiServiceUtilsCreateGithubRepo.getResourceFromDocument(
          document, CreateGithubRepo.class);

      // Ensure the validity of the request.
      verifyRequest(createGithubRepo);

      // Decrypt the github token passed in as a cookie.
      final String decryptedGithubToken = cryptoUtils.decrypt(githubToken, githubEncryption,
          githubSalt);

      // Ensure we have the required scopes
      verifyScopes(decryptedGithubToken);

      // Get the existing repo, or create a new one.
      createRepo(decryptedGithubToken, createGithubRepo);

      // Create the secrets
      createSecrets(decryptedGithubToken, createGithubRepo);

      // The empty repo needs a file pushed to ensure the GitHub client can find the default branch.
      populateInitialFile(decryptedGithubToken, createGithubRepo);

      // Download and extract the template zip file
      final String templateDir = Failsafe.with(RETRY_POLICY)
          .get(() -> downloadTemplate(createGithubRepo));

      // Commit the files
      commitFiles(decryptedGithubToken, createGithubRepo, templateDir);

      // return the details of the new repo
      return jsonApiServiceUtilsCreateGithubRepo.respondWithResource(CreateGithubRepo
          .builder()
          .id(createGithubRepo.getGithubOwner() + "/" + createGithubRepo.getGithubRepository())
          .githubOwner(createGithubRepo.getGithubOwner())
          .githubRepository(createGithubRepo.getGithubRepository())
          .build());
    } catch (final ClientWebApplicationException ex) {
      Log.error(microserviceNameFeature.getMicroserviceName() + "-ExternalRequest-Failed "
          + ex.getResponse().readEntity(String.class), ex);
      throw new InvalidInputException();
    } catch (final InvalidInputException ex) {
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

  private String downloadTemplate(final CreateGithubRepo createGithubRepo)
      throws DocumentSerializationException, IOException {

    final GenerateTemplate generateTemplate = GenerateTemplate.builder()
        .id("")
        .generator(createGithubRepo.getGenerator())
        .options(createGithubRepo.getOptions())
        .build();

    final String body = new String(jsonApiConverter.buildResourceConverter().writeDocument(
        new JSONAPIDocument<>(generateTemplate)));

    try (final TemporaryResources temp = new TemporaryResources()) {
      final Path zipFile = downloadTemplateToTempFile(body, temp);
      return extractZipToTempDir(zipFile, temp).toString();
    }
  }

  private Path downloadTemplateToTempFile(final String body, final TemporaryResources temp)
      throws IOException {
    try (final Response response = generateTemplateClient.generateTemplate(body, null, null)) {
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

  private void commitFiles(final String githubToken, final CreateGithubRepo createGithubRepo,
      final String path) throws IOException {
    final Path inputPath = Paths.get(path);

    final GitHub gitHub = new GitHubBuilder()
        .withOAuthToken(githubToken)
        /*
          Note that we must define a connector here, as the GitHubConnectorSubstitution removed
          the default value from native builds to fix a compilation error.
         */
        .withConnector(DefaultGitHubConnector.create())
        .build();

    // start with a Repository ref
    final GHRepository repo = gitHub.getRepository(
        createGithubRepo.getGithubOwner() + "/" + createGithubRepo.getGithubRepository());

    // get a sha to represent the root branch to start your commit from.
    // this can come from any number of classes:
    //   GHBranch, GHCommit, GHTree, etc...

    // for this example, we'll start from the main branch
    final GHBranch masterBranch = repo.getBranch("main");

    // get a tree builder object to build up into the commit.
    // the base of the tree will be the master branch
    GHTreeBuilder treeBuilder = repo.createTree().baseTree(masterBranch.getSHA1());

    // loop over all the files and add them to the treebuilder.
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

    /*
      Perform the commit. Note that if you get a 404 here uploading a workflow file, the github token
      may not have the "workflow" scope. See
      https://stackoverflow.com/questions/68064458/how-to-upload-content-via-github-api-to-hidden-folder-name.
    */
    final GHCommit commit = repo.createCommit()
        // base the commit on the tree we built
        .tree(treeBuilder.create().getSha())
        // set the parent of the commit as the master branch
        .parent(masterBranch.getSHA1())
        .message("App Builder repo population")
        .create();

    // update the main branch
    repo.getRef("heads/main").updateTo(commit.getSHA1());
  }

  private boolean fileIsExecutable(final File file) {
    return "mvnw".equals(file.getName());
  }

  private boolean pathHasIgnoreDirectory(final String path) {
    return StreamSupport.stream(Paths.get(path).spliterator(), false)
        .map(Path::toString)
        .anyMatch(p -> ArrayUtils.indexOf(IGNORE_PATHS, p) != -1);
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

  /**
   * We need to create an initial file in the repo to then allow the Git client to upload the rest.
   */
  private void populateInitialFile(final String decryptedGithubToken,
      final CreateGithubRepo createGithubRepo) {
    try {
      gitHubClient.getFile(
          "token " + decryptedGithubToken,
          createGithubRepo.getGithubOwner(),
          createGithubRepo.getGithubRepository(),
          "README.md");
    } catch (ClientWebApplicationException ex) {
      if (ex.getResponse().getStatus() == 404) {
        gitHubClient.createFile(
            GithubFile.builder()
                .content(Base64.getEncoder().encodeToString(
                    ("# App Builder\n"
                        + "This repo was populated by the Octopus App Builder tool. The directory structure is shown below:\n\n"
                        + "* `.github/workflows`: GitHub Action Workflows that populate a cloud Octopus instance, build and deploy the sample code, and initiate a deployment in Octopus.\n"
                        + "* `github`: Composable GitHub Actions that are called by the workflow files.\n"
                        + "* `terraform`: Terraform templates used to create cloud resources and populate the Octopus cloud instance.\n"
                        + "* `java`: The sample Java application.\n"
                        + "* `js`: The sample JavaScript application.\n"
                        + "* `dotnet`: The sample DotNET application.")
                        .getBytes(StandardCharsets.UTF_8)))
                .message("Adding the initial marker file")
                .branch("main")
                .build(),
            "token " + decryptedGithubToken,
            createGithubRepo.getGithubOwner(),
            createGithubRepo.getGithubRepository(),
            "README.md"
        );
      }
    }
  }

  private void createSecrets(final String decryptedGithubToken,
      final CreateGithubRepo createGithubRepo) throws IOException {
    // Create the sodium key.
    final GitHubPublicKey publicKey = gitHubClient.getPublicKey("token " + decryptedGithubToken,
        createGithubRepo.getGithubOwner(), createGithubRepo.getGithubRepository());

    // Add the repository secrets.
    if (createGithubRepo.getSecrets() != null) {
      for (final Secret secret : createGithubRepo.getSecrets()) {
        if (StringUtils.isBlank(secret.getName())) {
          continue;
        }

        final String secretValue = secret.isEncrypted()
            ? asymmetricDecryptor.decrypt(secret.getValue(), privateKeyBase64)
            : secret.getValue();

        // Create the Sodium secret box
        try (final SecretBox box = SecretBox.encrypt(
            SecretBox.key(Base64.getDecoder().decode(publicKey.getKey())),
            StringUtils.defaultIfEmpty(secretValue, ""))) {

          // extract the encrypted value
          try (var out = new ByteArrayOutputStream()) {
            box.writeTo(out);
            out.flush();

            // send the encrypted value
            gitHubClient.createSecret(
                GitHubSecret.builder()
                    .encryptedValue(Base64.getEncoder().encodeToString(out.toByteArray()))
                    .keyId(publicKey.getKeyId())
                    .build(),
                "token " + decryptedGithubToken,
                createGithubRepo.getGithubOwner(),
                createGithubRepo.getGithubRepository(),
                secret.getName());
          }
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

    throw new InvalidInputException(
        violations.stream().map(cv -> cv.getMessage()).collect(Collectors.joining(", ")));
  }
}
