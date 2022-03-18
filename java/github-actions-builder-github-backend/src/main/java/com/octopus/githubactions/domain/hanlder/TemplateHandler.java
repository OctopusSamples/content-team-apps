package com.octopus.githubactions.domain.hanlder;

import static org.jboss.logging.Logger.Level.DEBUG;

import com.google.common.io.Resources;
import com.octopus.builders.PipelineBuilder;
import com.octopus.encryption.AsymmetricEncryptor;
import com.octopus.encryption.CryptoUtils;
import com.octopus.features.MicroserviceNameFeature;
import com.octopus.githubactions.GlobalConstants;
import com.octopus.githubactions.application.lambda.PipelineLambda;
import com.octopus.githubactions.domain.audits.AuditGenerator;
import com.octopus.githubactions.domain.entities.Audit;
import com.octopus.githubactions.domain.entities.GitHubEmail;
import com.octopus.githubactions.domain.entities.GithubUserLoggedInForFreeToolsEventV1;
import com.octopus.githubactions.domain.servicebus.ServiceBusMessageGenerator;
import com.octopus.githubactions.infrastructure.client.GitHubUser;
import com.octopus.repoclients.RepoClient;
import com.octopus.repoclients.RepoClientFactory;
import io.quarkus.logging.Log;
import io.vavr.control.Try;
import java.util.Base64;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

/**
 * The template generation logic lives here.
 */
@ApplicationScoped
public class TemplateHandler {

  private static final Logger LOG = Logger.getLogger(PipelineLambda.class.toString());

  @ConfigProperty(name = "github.encryption")
  String githubEncryption;

  @ConfigProperty(name = "github.salt")
  String githubSalt;

  @Inject
  RepoClientFactory repoClientFactory;

  @Inject
  Instance<PipelineBuilder> builders;

  @Inject
  CryptoUtils cryptoUtils;

  @Inject
  AsymmetricEncryptor asymmetricEncryptor;

  @Inject
  AuditGenerator auditGenerator;

  @Inject
  ServiceBusMessageGenerator serviceBusMessageGenerator;

  @RestClient
  GitHubUser gitHubUser;

  @Inject
  MicroserviceNameFeature microserviceNameFeature;

  /**
   * Generate a github repo.
   *
   * @param repo                 The repo URL.
   * @param sessionCookie        The session cookie holding the GitHub access token.
   * @param routingHeaders       The "Routing" headers.
   * @param dataPartitionHeaders The "Data-Partition" headers.
   * @param authHeaders          The "Authorization" headers.
   * @return The response code and body.
   */
  public SimpleResponse generatePipeline(
      @NonNull final String repo,
      final String sessionCookie,
      final String xray,
      @NonNull final String routingHeaders,
      @NonNull final String dataPartitionHeaders,
      @NonNull final String authHeaders) {
    LOG.log(DEBUG, "PipelineLambda.generatePipeline(String)");
    if (StringUtils.isBlank(repo)) {
      throw new IllegalArgumentException("repo can not be blank");
    }

    final String auth = sessionCookie == null
        ? ""
        : cryptoUtils.decrypt(sessionCookie, githubEncryption, githubSalt);

    // Log the users emails details
    if (StringUtils.isNotBlank(auth)) {
      Try.of(() -> gitHubUser.publicEmails("token " + auth))
          .onSuccess(emails -> recordEmailInOctofront(
              auth,
              xray,
              emails,
              routingHeaders,
              dataPartitionHeaders,
              authHeaders))
          .onSuccess(emails -> auditEmail(
              auth,
              xray,
              emails,
              routingHeaders,
              dataPartitionHeaders,
              authHeaders));
    }

    final RepoClient accessor = repoClientFactory.buildRepoClient(repo, auth);

    return checkForPublicRepo(accessor)
        .orElse(buildPipeline(accessor, xray, routingHeaders, dataPartitionHeaders, authHeaders));
  }

  /**
   * Query the users email addresses, encrypt them, and log them to the audit.
   *
   * @param token                The GitHub access token.
   * @param routingHeaders       The routing headers.
   * @param dataPartitionHeaders The data-partition headers.
   * @param authHeaders          The authorization headers.
   */
  private void auditEmail(final String token,
      final String xray,
      final GitHubEmail[] emails,
      @NonNull final String routingHeaders,
      @NonNull final String dataPartitionHeaders,
      @NonNull final String authHeaders) {

    // We may not have a token to use.
    if (StringUtils.isEmpty(token)) {
      return;
    }

    try {

      final String publicKey = Base64.getEncoder()
          .encodeToString(Resources.toByteArray(Resources.getResource("public_key.der")));

      // Log first to the audit service
      for (final GitHubEmail email : emails) {
        final String encryptedEmail = asymmetricEncryptor.encrypt(email.getEmail(), publicKey);

        auditGenerator.createAuditEvent(new Audit(
                microserviceNameFeature.getMicroserviceName(),
                GlobalConstants.CREATED_TEMPLATE_FOR_ACTION,
                encryptedEmail,
                true,
                false),
            xray,
            routingHeaders,
            dataPartitionHeaders,
            authHeaders);
      }
    } catch (final Exception ex) {
      Log.error(
          microserviceNameFeature.getMicroserviceName() + "-Audit-RecordEmailFailed",
          ex);
    }
  }

  /**
   * Query the users email addresses, encrypt them, and log them to Octofront.
   *
   * @param token                The GitHub access token.
   * @param routingHeaders       The routing headers.
   * @param dataPartitionHeaders The data-partition headers.
   * @param authHeaders          The authorization headers.
   */
  private void recordEmailInOctofront(final String token,
      final String xray,
      final GitHubEmail[] emails,
      @NonNull final String routingHeaders,
      @NonNull final String dataPartitionHeaders,
      @NonNull final String authHeaders) {

    // We may not have a token to use.
    if (StringUtils.isEmpty(token)) {
      return;
    }

    // Log second to the Azure service bus proxy service
    try {
      for (final GitHubEmail email : emails) {
        serviceBusMessageGenerator.sendLoginMessage(
            GithubUserLoggedInForFreeToolsEventV1.builder()
                .emailAddress(email.getEmail())
                .build(),
            xray,
            routingHeaders,
            dataPartitionHeaders,
            authHeaders);

      }
    } catch (final Exception ex) {
      Log.error(
          microserviceNameFeature.getMicroserviceName() + "-ServiceBus-RecordLoginFailed",
          ex);
    }
  }

  private SimpleResponse buildPipeline(
      @NonNull final RepoClient accessor,
      final String xray,
      @NonNull final String routingHeaders,
      @NonNull final String dataPartitionHeaders,
      @NonNull final String authHeaders) {
    // Get the builder
    final Optional<PipelineBuilder> builder = builders.stream()
        .sorted((o1, o2) -> o2.getPriority().compareTo(o1.getPriority()))
        .parallel()
        .filter(b -> b.canBuild(accessor))
        .findFirst();

    // Write an audit message
    builder.ifPresent(b ->
        auditGenerator.createAuditEvent(new Audit(
                microserviceNameFeature.getMicroserviceName(),
                GlobalConstants.CREATED_TEMPLATE_ACTION,
                b.getName()),
            xray,
            routingHeaders,
            dataPartitionHeaders,
            authHeaders)
    );

    // Return the template
    return builder
        .map(b -> b.generate(accessor))
        .map(b -> new SimpleResponse(200, b))
        .orElse(new SimpleResponse(200, "No suitable builders were found.\n"
            + "This can happen if no recognised project files were found in the root directory.\n"
            + "You may still be able to use one of the sample projects from the main page, and customize it to suit your project.\n"
            + "Click the heading in the top left corner to return to the main page.\n"));
  }

  /**
   * If the repo is in accessible it is either because it does not exist, or is a private repo that
   * requires authentication. We make the decision here based on the presence of the session
   * cookie.
   */
  public Optional<SimpleResponse> checkForPublicRepo(@NonNull final RepoClient accessor) {
    if (!accessor.testRepo()) {
      if (accessor.hasAccessToken()) {
        return Optional.of(new SimpleResponse(
            404,
            accessor.getRepo()
                + " does not appear to be an accessible GitHub repository. Please try a different URL."));
      } else {
        return Optional.of(new SimpleResponse(
            401,
            accessor.getRepo()
                + " does not appear to be a public GitHub repository. You must login to GitHub."));
      }
    }

    return Optional.empty();
  }
}
