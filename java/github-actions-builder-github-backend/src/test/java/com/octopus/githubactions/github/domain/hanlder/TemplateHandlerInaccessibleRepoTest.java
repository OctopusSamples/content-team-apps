package com.octopus.githubactions.github.domain.hanlder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.octopus.encryption.CryptoUtils;
import com.octopus.githubactions.github.domain.TestingProfile;
import com.octopus.githubactions.github.domain.audits.AuditGenerator;
import com.octopus.githubactions.github.domain.entities.GitHubEmail;
import com.octopus.githubactions.github.domain.entities.Utms;
import com.octopus.githubactions.github.domain.servicebus.ServiceBusMessageGenerator;
import com.octopus.githubactions.github.infrastructure.client.GitHubUser;
import com.octopus.oauth.OauthClientCredsAccessor;
import com.octopus.repoclients.RepoClient;
import com.octopus.repoclients.RepoClientFactory;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

/**
 * This test verifies the response when the attempt to read a repo is denied with an access token.
 */
@QuarkusTest
@TestProfile(TestingProfile.class)
public class TemplateHandlerInaccessibleRepoTest {

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
    final RepoClient repoClient = Mockito.mock(RepoClient.class);
    when(repoClient.testRepo()).thenReturn(false);
    when(repoClient.hasAccessToken()).thenReturn(true);

    Mockito.when(repoClientFactory.buildRepoClient(any(), any()))
        .thenReturn(repoClient);
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

    assertEquals(404, response.getCode());
  }
}
