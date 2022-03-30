package com.octopus.githubactions.application.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.common.collect.ImmutableMap;
import com.octopus.encryption.CryptoUtils;
import com.octopus.githubactions.domain.TestingProfile;
import com.octopus.githubactions.domain.audits.AuditGenerator;
import com.octopus.githubactions.domain.entities.GitHubEmail;
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
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class PipelineLambdaTest {
  private static final String ENDPOINT = "/api/pipeline/github/generate";
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
  GitHubUser gitHubUser;

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
    Mockito.when(gitHubUser.publicEmails(any()))
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
        getContext()
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
        getContext()
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
        getContext()
    );

    assertEquals(201, response.getStatusCode());
  }

  private Context getContext() {
    return new Context() {
      @Override
      public String getAwsRequestId() {
        return null;
      }

      @Override
      public String getLogGroupName() {
        return null;
      }

      @Override
      public String getLogStreamName() {
        return null;
      }

      @Override
      public String getFunctionName() {
        return null;
      }

      @Override
      public String getFunctionVersion() {
        return null;
      }

      @Override
      public String getInvokedFunctionArn() {
        return null;
      }

      @Override
      public CognitoIdentity getIdentity() {
        return null;
      }

      @Override
      public ClientContext getClientContext() {
        return null;
      }

      @Override
      public int getRemainingTimeInMillis() {
        return 0;
      }

      @Override
      public int getMemoryLimitInMB() {
        return 0;
      }

      @Override
      public LambdaLogger getLogger() {
        return null;
      }
    };
  }
}
