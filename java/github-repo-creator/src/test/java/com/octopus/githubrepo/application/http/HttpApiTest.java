package com.octopus.githubrepo.application.http;

import static io.restassured.RestAssured.given;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.githubrepo.BaseTest;
import com.octopus.githubrepo.domain.entities.CreateGithubRepo;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.response.ValidatableResponse;
import java.util.Objects;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpApiTest extends BaseTest {

  private static final String API_ENDPOINT = "/api/customers";
  private static final String HEALTH_ENDPOINT = "/health/customers";

  @Inject ResourceConverter resourceConverter;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @BeforeEach
  public void beforeEach() {
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(true);
  }

  @Test
  public void failWithMissingContentTypeForPost() throws DocumentSerializationException {
        given()
            .accept("application/vnd.api+json")
            .header("data-partition", "main")
            .when()
            .body(
                resourceToResourceDocument(
                    resourceConverter, createResource("testCreateAndGetResource")))
            .post(API_ENDPOINT)
            .then()
            .statusCode(415);
  }

  @Test
  public void failWithoutPlainAcceptForPost() throws DocumentSerializationException {
        given()
            .accept("application/vnd.api+json; something")
            .header("data-partition", "main")
            .contentType("application/vnd.api+json")
            .when()
            .body(
                resourceToResourceDocument(
                    resourceConverter, createResource("testCreateAndGetResource")))
            .post(API_ENDPOINT)
            .then()
            .statusCode(406);
  }

  @Test
  public void failWithoutPlainAcceptForGet() {
        given()
            .accept("application/vnd.api+json; something")
            .when()
            .get(API_ENDPOINT + "/1")
            .then()
            .statusCode(406);
  }

  @Test
  public void failWithoutPlainAcceptForGetAll() {
        given()
            .accept("application/vnd.api+json; something")
            .when()
            .get(API_ENDPOINT)
            .then()
            .statusCode(406);
  }

  @Test
  public void passWithNoAcceptForGetAll() {
        given()
            .when()
            .get(API_ENDPOINT)
            .then()
            .statusCode(200);
  }

  @Test
  public void testCreateWithoutBody() {
        given()
            .accept("application/vnd.api+json")
            .header("data-partition", "main")
            .contentType("application/vnd.api+json")
            .when()
            .body("{}")
            .post(API_ENDPOINT)
            .then()
            .statusCode(400);
  }

  @Test
  public void testGetMissingEntity() {
    given()
        .accept("application/vnd.api+json")
        .header("data-partition", "main")
        .contentType("application/vnd.api+json")
        .when()
        .get(API_ENDPOINT + "/100000000000")
        .then()
        .statusCode(404);
  }

  @Test
  public void testHealthPostItem() {
    given()
        .when()
        .get(HEALTH_ENDPOINT + "/POST")
        .then()
        .statusCode(200);
  }

}
