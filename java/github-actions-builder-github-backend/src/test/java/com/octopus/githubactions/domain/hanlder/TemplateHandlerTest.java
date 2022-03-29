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
import com.octopus.repoclients.RepoClientFactory;
import com.octopus.test.repoclients.MavenTestRepoClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import io.vavr.control.Try;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

@QuarkusTest
@TestProfile(TestingProfile.class)
public class TemplateHandlerTest {

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
        .thenReturn(new MavenTestRepoClient(REPO, false));
    Mockito.when(oauthClientCredsAccessor.getAccessToken(any()))
        .thenReturn(Try.of(() -> "accesstoken"));
    Mockito.when(gitHubUser.publicEmails(any()))
        .thenReturn(new GitHubEmail[]{GitHubEmail.builder().email(TEST_EMAIL).build()});
    Mockito.when(cryptoUtils.decrypt(any(), any(), any())).thenReturn("decrypted");
    doNothing().when(auditGenerator).createAuditEvent(any(), any(), any(), any(), any());

    /*
      We expect the email address in the service bus message to match the one returned by the mocked
      GitHubUser rest client.
     */
    doAnswer(invocation -> {
      final GithubUserLoggedInForFreeToolsEventV1 message = invocation.getArgument(0);
      final String xray = invocation.getArgument(1);
      assertEquals(TEST_EMAIL, message.getEmailAddress());
      assertEquals(XRAY, xray);
      assertEquals("content", message.getUtmParameters().get("utm_content"));
      assertEquals("term", message.getUtmParameters().get("utm_term"));
      assertEquals("medium", message.getUtmParameters().get("utm_medium"));
      assertEquals("source", message.getUtmParameters().get("utm_source"));
      assertEquals("campaign", message.getUtmParameters().get("utm_campaign"));
      return null;
    }).when(serviceBusMessageGenerator).sendLoginMessage(any(), any(), any(), any(), any());
  }

  @ParameterizedTest
  @ValueSource(strings = {"sessioncookie", ""})
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

    assertEquals(200, response.getCode());
  }

  @Test
  public void testNullArguments() {
    assertThrows(NullPointerException.class,
        () -> templateHandler.generatePipeline(
            null,
            "",
            "",
            "",
            "",
            "",
            Utms.builder().build()));

    assertThrows(IllegalArgumentException.class,
        () -> templateHandler.generatePipeline(
            "",
            "",
            "",
            "",
            "",
            "",
            Utms.builder().build()));

    assertThrows(NullPointerException.class,
        () -> templateHandler.generatePipeline(
            "",
            "",
            "",
            null,
            "",
            "",
            Utms.builder().build()));

    assertThrows(NullPointerException.class,
        () -> templateHandler.generatePipeline(
            "",
            "",
            "",
            "",
            null,
            "",
            Utms.builder().build()));

    assertThrows(NullPointerException.class,
        () -> templateHandler.generatePipeline(
            "",
            "",
            "",
            "",
            "",
            null,
            Utms.builder().build()));

    assertThrows(NullPointerException.class,
        () -> templateHandler.generatePipeline(
            "",
            "",
            "",
            "",
            "",
            "",
            null));
  }
}
