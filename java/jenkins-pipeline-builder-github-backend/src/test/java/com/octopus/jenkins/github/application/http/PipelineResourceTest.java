package com.octopus.jenkins.github.application.http;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import com.octopus.encryption.CryptoUtils;
import com.octopus.jenkins.github.GlobalConstants;
import com.octopus.jenkins.github.domain.TestingProfile;
import com.octopus.jenkins.github.domain.audits.AuditGenerator;
import com.octopus.jenkins.github.domain.entities.GitHubEmail;
import com.octopus.jenkins.github.domain.servicebus.ServiceBusMessageGenerator;
import com.octopus.jenkins.github.infrastructure.client.GitHubApi;
import com.octopus.oauth.OauthClientCredsAccessor;
import com.octopus.repoclients.RepoClientFactory;
import com.octopus.test.repoclients.MavenTestRepoClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import io.vavr.control.Try;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class PipelineResourceTest {

  private static final String ENDPOINT = "/api/pipeline/jenkins/generate";
  private static final String REPO = "https://github.com/OctopusSamples/RandomQuotes-Java";
  private static final String TEST_EMAIL = "a@example.org";
  private static final String XRAY = "sample_xray";

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
    Mockito.when(cryptoUtils.decrypt(any(), any(), any())).thenReturn("decrypted");
    doNothing().when(auditGenerator).createAuditEvent(any(), any(), any(), any(), any());
    doNothing().when(serviceBusMessageGenerator)
        .sendLoginMessage(any(), any(), any(), any(), any());
  }

  @Test
  public void testHttpInterface() {
    given()
        .queryParam("repo", REPO)
        .header(GlobalConstants.AMAZON_TRACE_ID_HEADER, XRAY)
        .header(GlobalConstants.ROUTING_HEADER, "")
        .header(GlobalConstants.DATA_PARTITION, "")
        .header(GlobalConstants.AUTHORIZATION_HEADER, "")
        .when()
        .get(ENDPOINT)
        .then()
        .statusCode(401);
  }

  @Test
  public void badRequest() {
    given()
        .queryParam("repo", "")
        .header(GlobalConstants.AMAZON_TRACE_ID_HEADER, XRAY)
        .header(GlobalConstants.ROUTING_HEADER, "")
        .header(GlobalConstants.DATA_PARTITION, "")
        .header(GlobalConstants.AUTHORIZATION_HEADER, "")
        .when()
        .get(ENDPOINT)
        .then()
        .statusCode(400);
  }
}
