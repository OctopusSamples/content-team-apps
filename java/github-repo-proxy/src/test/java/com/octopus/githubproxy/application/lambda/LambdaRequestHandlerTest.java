package com.octopus.githubproxy.application.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.encryption.CryptoUtils;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.githubproxy.TestingProfile;
import com.octopus.githubproxy.application.Paths;
import com.octopus.githubproxy.domain.entities.Repo;
import com.octopus.githubproxy.domain.entities.RepoOwner;
import com.octopus.githubproxy.domain.entities.WorkflowRuns;
import com.octopus.githubproxy.infrastructure.clients.GitHubClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class LambdaRequestHandlerTest {

  @Inject
  LambdaRequestHanlder api;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @InjectMock
  @RestClient
  GitHubClient gitHubClient;

  @InjectMock
  CryptoUtils cryptoUtils;

  @BeforeEach
  public void beforeEach() {
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(true);

    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(true);

    final Response missingResponse = Mockito.mock(Response.class);
    Mockito.when(missingResponse.getStatus()).thenReturn(404);
    Mockito.when(missingResponse.getStatusInfo()).thenReturn(Mockito.mock(StatusType.class));

    // Mock the response from the GitHub API to either return a repo or a 404
    Mockito.when(gitHubClient.getRepo(any(), any(), any())).thenAnswer(invocation -> {
      final String owner = invocation.getArgument(0, String.class);
      final String repo = invocation.getArgument(1, String.class);

      if ("owner".equals(owner) && "repo".equals(repo)) {
        return Uni.createFrom().item(
            Repo.builder().owner(RepoOwner.builder().login("owner").build()).name("repo")
                .build());
      }

      return Uni.createFrom().failure(new ClientWebApplicationException(missingResponse));
    });
    Mockito.when(gitHubClient.getWorkflowRuns(any(), any(), any())).thenReturn(Uni.createFrom().item(WorkflowRuns.builder().build()));

    Mockito.when(cryptoUtils.decrypt(any(), any(), any())).thenReturn("decrypted");
  }

  @Test
  public void assertEventIsNotNull() {
    assertThrows(NullPointerException.class, () -> {
      api.handleRequest(null, Mockito.mock(Context.class));
    });

    assertThrows(NullPointerException.class, () -> {
      api.handleRequest(new APIGatewayProxyRequestEvent(), null);
    });
  }

  @Test
  public void testGetEntity() throws UnsupportedEncodingException {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHeaders(new HashMap<>() {
      {
        put("Accept", "application/vnd.api+json");
      }
    });
    apiGatewayProxyRequestEvent.setHttpMethod("GET");
    apiGatewayProxyRequestEvent.setPath(Paths.API_ENDPOINT + "/"
        + URLEncoder.encode("https://api.github.com/repos/owner/repo", StandardCharsets.UTF_8.toString()));
    final APIGatewayProxyResponseEvent postResponse = api.handleRequest(apiGatewayProxyRequestEvent,
        Mockito.mock(Context.class));
    assertEquals(200, postResponse.getStatusCode());
  }

  @Test
  public void testMissingEntity() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHeaders(new HashMap<>() {
      {
        put("Accept", "application/vnd.api+json");
      }
    });
    apiGatewayProxyRequestEvent.setHttpMethod("GET");
    apiGatewayProxyRequestEvent.setPath(Paths.API_ENDPOINT + "/owner%2Fmyrepo");
    final APIGatewayProxyResponseEvent postResponse = api.handleRequest(apiGatewayProxyRequestEvent,
        Mockito.mock(Context.class));
    assertEquals(404, postResponse.getStatusCode());
  }

  @Test
  public void testMissingPath() {
    final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent();
    apiGatewayProxyRequestEvent.setHeaders(new HashMap<>() {
      {
        put("Accept", "application/vnd.api+json");
      }
    });
    apiGatewayProxyRequestEvent.setHttpMethod("GET");
    apiGatewayProxyRequestEvent.setPath("/api/blah");
    final APIGatewayProxyResponseEvent postResponse = api.handleRequest(apiGatewayProxyRequestEvent,
        Mockito.mock(Context.class));
    assertEquals(404, postResponse.getStatusCode());
  }

  @Test
  public void testGetMissingEntity() {
    final APIGatewayProxyRequestEvent getApiGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent();
    getApiGatewayProxyRequestEvent.setHeaders(new HashMap<>() {
      {
        put("Accept", "application/vnd.api+json");
      }
    });
    getApiGatewayProxyRequestEvent.setHttpMethod("GET");
    getApiGatewayProxyRequestEvent.setPath(Paths.API_ENDPOINT + "/10000000000000000000");
    final APIGatewayProxyResponseEvent getResponse = api.handleRequest(
        getApiGatewayProxyRequestEvent, Mockito.mock(Context.class));
    assertEquals(404, getResponse.getStatusCode());
  }
}
