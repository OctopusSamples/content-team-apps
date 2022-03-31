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
import com.octopus.jenkins.github.infrastructure.client.GitHubUser;
import com.octopus.oauth.OauthClientCredsAccessor;
import com.octopus.repoclients.RepoClient;
import com.octopus.repoclients.RepoClientFactory;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import io.vavr.control.Try;
import java.util.List;
import lombok.NonNull;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

/**
 * This test verifies the response when the git repo could not be accessed.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class PipelineResourceUnauthorizedTest {

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

          /**
           * Report that the access token has not been set.
           * @return always false
           */
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

          /**
           * Report that we can not access the repo.
           * @return always false
           */
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
    doNothing().when(serviceBusMessageGenerator)
        .sendLoginMessage(any(), any(), any(), any(), any());
  }

  /**
   * When the repo can not be accessed and an accesstoken has not been provided, we report
   * that the access is aunauthorized.
   */
  @Test
  public void testHttpInterfaceUnauthorized() {
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
}
