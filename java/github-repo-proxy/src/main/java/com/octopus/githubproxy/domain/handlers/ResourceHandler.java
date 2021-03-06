package com.octopus.githubproxy.domain.handlers;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.SerializationSettings;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.encryption.CryptoUtils;
import com.octopus.exceptions.EntityNotFoundException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.features.AdminJwtClaimFeature;
import com.octopus.features.AdminJwtGroupFeature;
import com.octopus.githubproxy.domain.entities.GitHubRepo;
import com.octopus.githubproxy.domain.entities.GitHubRepoMeta;
import com.octopus.githubproxy.domain.entities.WorkflowRun;
import com.octopus.githubproxy.domain.entities.WorkflowRuns;
import com.octopus.githubproxy.domain.entities.Repo;
import com.octopus.githubproxy.domain.entities.RepoId;
import com.octopus.githubproxy.domain.entities.GitHubWorkflowRun;
import com.octopus.githubproxy.domain.features.impl.DisableSecurityFeatureImpl;
import com.octopus.githubproxy.infrastructure.clients.GitHubClient;
import com.octopus.jwt.JwtInspector;
import com.octopus.jwt.JwtUtils;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vavr.control.Try;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.util.EntityUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

/**
 * Handlers take the raw input from the upstream service, like Lambda or a web server, convert the inputs to POJOs, apply the security rules, and then pass the
 * requests down to repositories.
 */
@ApplicationScoped
public class ResourceHandler {

  @Inject
  CryptoUtils cryptoUtils;

  @ConfigProperty(name = "github.encryption")
  String githubEncryption;

  @ConfigProperty(name = "github.salt")
  String githubSalt;

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

  @RestClient
  GitHubClient gitHubClient;

  /**
   * Returns the one resource that matches the supplied ID.
   *
   * @param id                   The ID of the resource to return.
   * @param dataPartitionHeaders The "data-partition" headers.
   * @return The matching resource.
   * @throws DocumentSerializationException Thrown if the entity could not be converted to a JSONAPI resource.
   */
  public Uni<String> getOne(@NonNull final String id,
      @NonNull final List<String> dataPartitionHeaders,
      final String authorizationHeader,
      final String serviceAuthorizationHeader,
      @NonNull final String githubToken) {
    if (!isAuthorized(authorizationHeader, serviceAuthorizationHeader)) {
      throw new UnauthorizedException();
    }

    // split the repo URL into the owner and repo
    final Optional<RepoId> repoId = RepoId.fromId(id);

    // If the ID was not valid, return a 404
    if (repoId.isEmpty()) {
      throw new EntityNotFoundException();
    }

    // Decrypt the github token passed in as a cookie.
    final String decryptedGithubToken = cryptoUtils.decrypt(githubToken, githubEncryption, githubSalt);

    // Attempt to get the repo
    return getRepo(repoId.get(), decryptedGithubToken)
        .flatMap(d -> getWorkflowRuns(d, repoId.get(), decryptedGithubToken))
        .map(Unchecked.function(d -> respondWithResource(
            GitHubRepo
                .builder()
                .id(URLDecoder.decode(id, StandardCharsets.UTF_8))
                .workflowRuns(d.runs)
                .meta(GitHubRepoMeta
                    .builder()
                    .browsableUrl("https://github.com/" + d.repo.getOwner().getLogin() + "/" + d.repo.getName())
                    .build())
                .owner(d.repo.getOwner().getLogin())
                .repo(d.repo.getName())
                .build())));
  }

  /**
   * Deal with upstream GitHub exceptions.
   *
   * @param ex The exception thrown when calling the upstream service.
   * @return The exception that needs to be rethrown.
   */
  private RuntimeException handleException(final ClientWebApplicationException ex) {
    if (ex.getResponse().getStatus() == 404) {
      return new EntityNotFoundException();
    } else {
      Log.error("HTTP Response: " + ex.getResponse().readEntity(String.class));
    }

    return ex;
  }

  private Uni<GitHubDetails> getRepo(final RepoId repoId, final String decryptedGithubToken) {
    return gitHubClient.getRepo(
            repoId.getOwner(),
            repoId.getRepo(),
            "token " + decryptedGithubToken)
        .map(GitHubDetails::new)
        .onFailure(ClientWebApplicationException.class)
        .transform(e -> handleException((ClientWebApplicationException) e));
  }

  private Uni<GitHubDetails> getWorkflowRuns(final GitHubDetails details, final RepoId repoId, final String decryptedGithubToken) {
    return gitHubClient.getWorkflowRuns(
            repoId.getOwner(),
            repoId.getRepo(),
            "token " + decryptedGithubToken)
        .map(this::convertWorkflowRuns)
        .map(w -> new GitHubDetails(details.repo, w))
        .onFailure(ClientWebApplicationException.class)
        .transform(e -> handleException((ClientWebApplicationException) e));
  }

  private List<GitHubWorkflowRun> convertWorkflowRuns(final WorkflowRuns workflowRuns) {
    if (workflowRuns == null || workflowRuns.getWorkflowRuns() == null) {
      return List.of();
    }

    return workflowRuns.getWorkflowRuns().stream()
        .map(x -> GitHubWorkflowRun.builder()
            .id(x.getId())
            .status(x.getStatus())
            .htmlUrl(x.getHtmlUrl())
            .runNumber(x.getRunNumber())
            .build())
        .collect(Collectors.toList());
  }

  private String respondWithResource(final GitHubRepo gitHubRepo)
      throws DocumentSerializationException {
    final JSONAPIDocument<GitHubRepo> document = new JSONAPIDocument<GitHubRepo>(gitHubRepo);
    final SerializationSettings serializationSettings = new SerializationSettings.Builder()
        .includeRelationship("workflowRuns")
        .build();
    return new String(resourceConverter.writeDocument(document, serializationSettings));
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

  private static class GitHubDetails {

    public Repo repo;
    public List<GitHubWorkflowRun> runs;
    public String branch;

    public GitHubDetails(Repo repo) {
      this.repo = repo;
    }

    public GitHubDetails(Repo repo, List<GitHubWorkflowRun> runs) {
      this.repo = repo;
      this.runs = runs;
    }

    public GitHubDetails(Repo repo, List<GitHubWorkflowRun> runs, String branch) {
      this.repo = repo;
      this.runs = runs;
      this.branch = branch;
    }
  }
}
