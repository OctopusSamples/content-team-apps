package com.octopus.githubproxy.application.http;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.octopus.encryption.CryptoUtils;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.githubproxy.TestingProfile;
import com.octopus.githubproxy.application.Paths;
import com.octopus.githubproxy.domain.entities.Repo;
import com.octopus.githubproxy.domain.entities.RepoOwner;
import com.octopus.githubproxy.infrastructure.clients.GitHubClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

/**
 * These tests verify the HTTP endpoints.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class JsonApiRootResourceTest {

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

    final Response missingResponse = Mockito.mock(Response.class);
    Mockito.when(missingResponse.getStatus()).thenReturn(404);
    Mockito.when(missingResponse.getStatusInfo()).thenReturn(Mockito.mock(StatusType.class));

    // Mock the response from the GitHub API to either return a repo or a 404
    Mockito.when(gitHubClient.getRepo(any(), any(), any())).thenAnswer(invocation -> {
      final String owner = invocation.getArgument(0, String.class);
      final String repo = invocation.getArgument(1, String.class);

      if ("owner".equals(owner) && "repo".equals(repo)) {
        return Repo
            .builder()
            .owner(RepoOwner.builder().login("owner").build())
            .name("repo").build();
      }

      throw new ClientWebApplicationException(missingResponse);
    });

    Mockito.when(cryptoUtils.decrypt(any(), any(), any())).thenReturn("decrypted");
  }

  @Test
  public void failWithoutPlainAcceptForGet() {
        given()
            .accept("application/vnd.api+json; something")
            .when()
            .get(Paths.API_ENDPOINT + "/1")
            .then()
            .statusCode(406);
  }

  @Test
  public void testGetMissingEntity() {
    given()
        .accept("application/vnd.api+json")
        .header("data-partition", "main")
        .cookie("GitHubUserSession", "blah")
        .contentType("application/vnd.api+json")
        .when()
        .get(Paths.API_ENDPOINT + "/100000000000")
        .then()
        .statusCode(404);
  }

  @Test
  public void testGetEntity() {
    given()
        .accept("application/vnd.api+json")
        .header("data-partition", "main")
        .cookie("GitHubUserSession", "blah")
        .contentType("application/vnd.api+json")
        .when()
        .get(Paths.API_ENDPOINT + "/owner%2Frepo")
        .then()
        .statusCode(200);
  }

  @Test
  public void testHealthGetItem() {
    given()
        .when()
        .get(Paths.HEALTH_ENDPOINT + "/x/GET")
        .then()
        .statusCode(200);
  }
}
