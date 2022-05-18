package com.octopus.jenkins.github.domain.hanlder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;

import com.octopus.encryption.CryptoUtils;
import com.octopus.jenkins.github.domain.TestingProfile;
import com.octopus.jenkins.github.domain.audits.AuditGenerator;
import com.octopus.jenkins.github.domain.entities.GitHubEmail;
import com.octopus.jenkins.github.domain.entities.GitHubUser;
import com.octopus.jenkins.github.domain.entities.GithubUserLoggedInForFreeToolsEventV1;
import com.octopus.jenkins.github.domain.entities.Utms;
import com.octopus.jenkins.github.domain.servicebus.ServiceBusMessageGenerator;
import com.octopus.jenkins.github.infrastructure.client.GitHubApi;
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
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

@QuarkusTest
@TestProfile(TestingProfile.class)
public class TemplateHandlerTest {

  private static final String REPO = "https://github.com/OctopusSamples/RandomQuotes-Java";
  private static final String TEST_EMAIL = "a@example.org";
  private static final String LOGIN = "login";
  private static final String NAME = "my name";
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
  GitHubApi gitHubApi;

  @InjectMock
  CryptoUtils cryptoUtils;

  @BeforeEach
  public void setup() {
    Mockito.when(repoClientFactory.buildRepoClient(any(), any()))
        .thenReturn(new MavenTestRepoClient(REPO, false));
    Mockito.when(oauthClientCredsAccessor.getAccessToken(any()))
        .thenReturn(Try.of(() -> "accesstoken"));
    Mockito.when(gitHubApi.publicEmails(any()))
        .thenReturn(new GitHubEmail[]{GitHubEmail.builder().email(TEST_EMAIL).build()});
    Mockito.when(gitHubApi.user(any()))
        .thenReturn(GitHubUser.builder().login(LOGIN).name(NAME).build());
    Mockito.when(cryptoUtils.decrypt(any(), any(), any())).thenReturn("decrypted");
    doNothing().when(auditGenerator).createAuditEvent(any(), any(), any(), any(), any());

    /*
      We expect the email address in the service bus message to match the one returned by the mocked
      GitHubUser rest client.
     */
    doAnswer(invocation -> {
      final GithubUserLoggedInForFreeToolsEventV1 message = invocation.getArgument(0);
      final String xray = invocation.getArgument(1);
      assertTrue(TEST_EMAIL.equals(message.getEmailAddress()) || StringUtils.isBlank(message.getEmailAddress()));
      assertTrue(LOGIN.equals(message.getGitHubUsername()) || StringUtils.isBlank(message.getGitHubUsername()));
      assertTrue("my".equals(message.getFirstName()) || StringUtils.isBlank(message.getFirstName()));
      assertTrue("name".equals(message.getLastName()) || StringUtils.isBlank(message.getLastName()));
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

    assertEquals(401, response.getCode());
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
