package com.octopus.githubproxy.application.http;

import static io.restassured.RestAssured.given;

import com.octopus.features.DisableSecurityFeature;
import com.octopus.githubproxy.application.Paths;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

/**
 * These tests verify the HTTP endpoints.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JsonApiRootResourceTest {

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @BeforeEach
  public void beforeEach() {
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(true);
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
  public void testHealthGetCollection() {
    given()
        .when()
        .get(Paths.HEALTH_ENDPOINT + "/GET")
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
