package com.octopus.githubactions.domain.hanlder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;

import com.octopus.encryption.CryptoUtils;
import com.octopus.githubactions.domain.audits.AuditGenerator;
import com.octopus.githubactions.domain.entities.GitHubEmail;
import com.octopus.githubactions.domain.entities.GithubUserLoggedInForFreeToolsEventV1;
import com.octopus.githubactions.domain.entities.Utms;
import com.octopus.githubactions.domain.servicebus.ServiceBusMessageGenerator;
import com.octopus.githubactions.infrastructure.client.GitHubUser;
import com.octopus.oauth.OauthClientCredsAccessor;
import com.octopus.repoclients.RepoClient;
import com.octopus.repoclients.RepoClientFactory;
import com.octopus.test.repoclients.MavenTestRepoClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import io.vavr.control.Try;
import java.util.List;
import javax.inject.Inject;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

/**
 * This test verifies the response when the attempt to read a repo is denied without an access token.
 */
@QuarkusTest
@TestProfile(TestingProfile.class)
public class TemplateHandlerPrivateRepoTest {

  private static final String REPO = "https://github.com/OctopusSamples/RandomQuotes-Java";
  private static final String TEST_EMAIL = "a@example.org";
  private static final String XRAY = "sample_xray";

  @Inject
  TemplateHandler templateHandler;

  @InjectMock
  RepoClientFactory repoClientFactory;

  @InjectMock
  AuditGenerator auditGenerator;

  @InjectMock
  ServiceBusMessageGenerator serviceBusMessageGenerator;

  @InjectMock
  OauthClientCredsAccessor oauthClientCredsAccessor;

  @InjectMock
  @RestClient
  GitHubUser gitHubUser;

  @InjectMock
  CryptoUtils cryptoUtils;

  @BeforeEach
  public void setup() {
    Mockito.when(repoClientFactory.buildRepoClient(any(), any()))
        .thenReturn(new RepoClient() {
          @Override
          public String getRepo() {
            return null;
          }

          @Override
          public boolean hasAccessToken() {
            return false;
          }

          @Override
          public Try<String> getFile(String path) {
            return null;
          }

          @Override
          public boolean testFile(String path) {
            return false;
          }

          @Override
          public Try<List<String>> getWildcardFiles(String path, int limit) {
            return null;
          }

          @Override
          public Try<Boolean> wildCardFileExist(@NonNull String path) {
            return null;
          }

          @Override
          public String getRepoPath() {
            return null;
          }

          @Override
          public List<String> getDefaultBranches() {
            return null;
          }

          @Override
          public Try<String> getRepoName() {
            return null;
          }

          @Override
          public boolean testRepo() {
            return false;
          }
        });
    Mockito.when(oauthClientCredsAccessor.getAccessToken(any()))
        .thenReturn(Try.of(() -> "accesstoken"));
    Mockito.when(gitHubUser.publicEmails(any()))
        .thenReturn(new GitHubEmail[]{GitHubEmail.builder().email(TEST_EMAIL).build()});
    Mockito.when(cryptoUtils.decrypt(any(), any(), any())).thenReturn("decrypted");
    doNothing().when(auditGenerator).createAuditEvent(any(), any(), any(), any(), any());
    doNothing().when(serviceBusMessageGenerator).sendLoginMessage(any(), any(), any(), any(), any());
  }

  @ParameterizedTest
  @ValueSource(strings = {"sessioncookie"})
  public void testTemplateCreation(final String sessionCookie) {
    final SimpleResponse response = templateHandler.generatePipeline(
        REPO,
        StringUtils.isEmpty(sessionCookie) ? null : sessionCookie,
        XRAY,
        "",
        "",
        "",
        Utms
            .builder()
            .content("content")
            .term("term")
            .medium("medium")
            .source("source")
            .campaign("campaign")
            .build());

    assertEquals(401, response.getCode());
  }
}
