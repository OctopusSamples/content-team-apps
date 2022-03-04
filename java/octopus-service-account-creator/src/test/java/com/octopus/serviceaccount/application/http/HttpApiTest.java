package com.octopus.serviceaccount.application.http;

import static io.restassured.RestAssured.given;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.serviceaccount.BaseTest;
import com.octopus.serviceaccount.domain.entities.ServiceAccount;
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
  public void testCreateAndGetAudit() throws DocumentSerializationException {
    final ValidatableResponse response =
        given()
            .accept("application/vnd.api+json")
            .header("data-partition", "main")
            .contentType("application/vnd.api+json")
            .when()
            .body(
                resourceToResourceDocument(
                    resourceConverter, createResource("testCreateAndGetResource")))
            .post(API_ENDPOINT)
            .then()
            .statusCode(200)
            .body(
                new LambdaMatcher<String>(
                    a -> getResourceFromDocument(resourceConverter, a) != null,
                    "Resource should be returned"));

    final ServiceAccount created =
        getResourceFromDocument(resourceConverter, response.extract().body().asString());

    given()
        .accept("application/vnd.api+json")
        .header("data-partition", "main")
        .when()
        .get(API_ENDPOINT)
        .then()
        .statusCode(200)
        .body(
            new LambdaMatcher<String>(
                a ->
                    getResourcesFromDocument(resourceConverter, a).stream()
                        .anyMatch(p -> Objects.equals(created.getId(), p.getId())),
                "Resource should be returned"));

    given()
        .accept("application/vnd.api+json")
        .header("data-partition", "main")
        .when()
        .get(API_ENDPOINT + "/" + created.getId())
        .then()
        .statusCode(200)
        .body(
            new LambdaMatcher<String>(
                a ->
                    getResourceFromDocument(resourceConverter, a)
                        .getName()
                        .equals(created.getName()),
                "Resource should be returned"));
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
