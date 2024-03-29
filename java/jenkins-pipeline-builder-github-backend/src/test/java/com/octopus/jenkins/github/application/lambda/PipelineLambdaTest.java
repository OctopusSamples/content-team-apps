package com.octopus.jenkins.github.application.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.common.collect.ImmutableMap;
import com.octopus.encryption.CryptoUtils;
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
import javax.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class PipelineLambdaTest {
  private static final String ENDPOINT = "/api/pipeline/jenkins/generate";
  private static final String REPO = "https://github.com/OctopusSamples/RandomQuotes-Java";
  private static final String TEST_EMAIL = "a@example.org";

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

  @Inject
  PipelineLambda pipelineLambda;

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
  public void testLambdaInterface() {
    final APIGatewayProxyResponseEvent response = pipelineLambda.handleRequest(
        new APIGatewayProxyRequestEvent()
            .withPath(ENDPOINT)
            .withQueryStringParameters(
                new ImmutableMap.Builder<String, String>().put("repo", REPO).build()),
        Mockito.mock(Context.class)
    );

    assertEquals(200, response.getStatusCode());
  }

  @Test
  public void testBadRequest() {
    final APIGatewayProxyResponseEvent response = pipelineLambda.handleRequest(
        new APIGatewayProxyRequestEvent()
            .withPath(ENDPOINT)
            .withQueryStringParameters(
                new ImmutableMap.Builder<String, String>().put("repo", "").build()),
        Mockito.mock(Context.class)
    );

    assertEquals(400, response.getStatusCode());
  }

  @Test
  public void testHealth() {
    final APIGatewayProxyResponseEvent response = pipelineLambda.handleRequest(
        new APIGatewayProxyRequestEvent()
            .withPath(ENDPOINT)
            .withQueryStringParameters(
                new ImmutableMap.Builder<String, String>().put("action", "health").build()),
        Mockito.mock(Context.class)
    );

    assertEquals(201, response.getStatusCode());
  }
}
