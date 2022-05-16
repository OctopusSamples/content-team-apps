package com.octopus.products.application.http;

import static io.restassured.RestAssured.given;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.products.BaseTest;
import com.octopus.products.application.Paths;
import com.octopus.products.domain.entities.Product;
import com.octopus.products.infrastructure.utilities.LiquidbaseUpdater;
import com.octopus.features.DisableSecurityFeature;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.response.ValidatableResponse;
import java.sql.SQLException;
import java.util.Objects;
import javax.inject.Inject;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

/**
 * These tests verify the HTTP endpoints.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JsonApiRootResourceGetAllTest extends BaseTest {

  @Inject
  LiquidbaseUpdater liquidbaseUpdater;

  @Inject ResourceConverter resourceConverter;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @BeforeEach
  public void beforeEach() {
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(true);
  }

  @BeforeAll
  public void setup() throws SQLException, LiquibaseException {
    liquidbaseUpdater.update();
  }

  @Test
  public void testCreateAndGetResource() throws DocumentSerializationException {
    final ValidatableResponse response =
        given()
            .accept("application/vnd.api+json")
            .header("data-partition", "main")
            .contentType("application/vnd.api+json")
            .when()
            .body(
                resourceToResourceDocument(
                    resourceConverter, createResource("testCreateAndGetResource")))
            .post(Paths.API_ENDPOINT)
            .then()
            .statusCode(201)
            .body(
                new LambdaMatcher<String>(
                    a -> getResourceFromDocument(resourceConverter, a) != null,
                    "Resource should be returned"));

    final Product created =
        getResourceFromDocument(resourceConverter, response.extract().body().asString());

    given()
        .accept("application/vnd.api+json")
        .header("data-partition", "main")
        .when()
        .get(Paths.API_ENDPOINT)
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
        .get(Paths.API_ENDPOINT + "/" + created.getId())
        .then()
        .statusCode(200)
        .body(
            new LambdaMatcher<String>(
                a -> getResourceFromDocument(resourceConverter, a).getName().equals(created.getName()),
                "Resource should be returned"));
  }

  @Test
  public void failToGetResourceInDifferentPartition() throws DocumentSerializationException {
    final ValidatableResponse response =
        given()
            .accept("application/vnd.api+json")
            .header("data-partition", "testing2")
            .contentType("application/vnd.api+json")
            .when()
            .body(
                resourceToResourceDocument(
                    resourceConverter, createResource("testCreateAndGetResource")))
            .post(Paths.API_ENDPOINT)
            .then()
            .statusCode(201);

    final Product created =
        getResourceFromDocument(resourceConverter, response.extract().body().asString());

    given()
        .accept("application/vnd.api+json")
        .header("data-partition", "testing")
        .when()
        .get(Paths.API_ENDPOINT + "/" + created.getId())
        .then()
        .statusCode(404);
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
            .post(Paths.API_ENDPOINT)
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
            .post(Paths.API_ENDPOINT)
            .then()
            .statusCode(406);
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
  public void failWithoutPlainAcceptForGetAll() {
        given()
            .accept("application/vnd.api+json; something")
            .when()
            .get(Paths.API_ENDPOINT)
            .then()
            .statusCode(406);
  }

  @Test
  public void passWithNoAcceptForGetAll() {
        given()
            .when()
            .get(Paths.API_ENDPOINT)
            .then()
            .statusCode(200);
  }

  @Test
  public void testFilterResults() throws DocumentSerializationException {
    final ValidatableResponse response =
        given()
            .accept("application/vnd.api+json")
            .header("data-partition", "main")
            .contentType("application/vnd.api+json")
            .when()
            .body(
                resourceToResourceDocument(
                    resourceConverter, createResource("testCreateAndGetResource")))
            .post(Paths.API_ENDPOINT)
            .then()
            .statusCode(201)
            .body(
                new LambdaMatcher<String>(
                    a -> getResourceFromDocument(resourceConverter, a.toString()) != null,
                    "Resource should be returned"));

    final Product created =
        getResourceFromDocument(resourceConverter, response.extract().body().asString());

    given()
        .accept("application/vnd.api+json")
        .header("data-partition", "main")
        .when()
        .queryParam("filter", "id==" + created.getId())
        .get(Paths.API_ENDPOINT)
        .then()
        .statusCode(200)
        .body(
            new LambdaMatcher<String>(
                a ->
                    getResourcesFromDocument(resourceConverter, a.toString()).stream()
                        .anyMatch(p -> Objects.equals(created.getId(), p.getId())),
                "Resource should be returned"));

    given()
        .accept("application/vnd.api+json")
        .header("data-partition", "main")
        .when()
        .queryParam("filter", "name==testCreateAndGetResource")
        .get(Paths.API_ENDPOINT)
        .then()
        .statusCode(200)
        .body(
            new LambdaMatcher<String>(
                a ->
                    getResourcesFromDocument(resourceConverter, a.toString()).stream()
                        .anyMatch(p -> Objects.equals(created.getId(), p.getId())),
                "Resource should be returned"));

    given()
        .accept("application/vnd.api+json")
        .header("data-partition", "main")
        .when()
        .queryParam("filter", "name!=blah")
        .get(Paths.API_ENDPOINT)
        .then()
        .statusCode(200)
        .body(
            new LambdaMatcher<String>(
                a ->
                    getResourcesFromDocument(resourceConverter, a.toString()).stream()
                        .anyMatch(p -> Objects.equals(created.getId(), p.getId())),
                "Resource should be returned"));

    given()
        .accept("application/vnd.api+json")
        .header("data-partition", "main")
        .when()
        .queryParam("filter", "name==test*")
        .get(Paths.API_ENDPOINT)
        .then()
        .statusCode(200)
        .body(
            new LambdaMatcher<String>(
                a ->
                    getResourcesFromDocument(resourceConverter, a.toString()).stream()
                        .anyMatch(p -> Objects.equals(created.getId(), p.getId())),
                "Resource should be returned"));

    given()
        .accept("application/vnd.api+json")
        .header("data-partition", "main")
        .when()
        .queryParam("filter", "name=in=(testCreateAndGetResource)")
        .get(Paths.API_ENDPOINT)
        .then()
        .statusCode(200)
        .body(
            new LambdaMatcher<String>(
                a ->
                    getResourcesFromDocument(resourceConverter, a.toString()).stream()
                        .anyMatch(p -> Objects.equals(created.getId(), p.getId())),
                "Resource should be returned"));

    given()
        .accept("application/vnd.api+json")
        .header("data-partition", "main")
        .when()
        .queryParam("filter", "id<" + (created.getId() + 1))
        .get(Paths.API_ENDPOINT)
        .then()
        .statusCode(200)
        .body(
            new LambdaMatcher<String>(
                a ->
                    getResourcesFromDocument(resourceConverter, a.toString()).stream()
                        .anyMatch(p -> Objects.equals(created.getId(), p.getId())),
                "Resource should be returned"));
  }

  @Test
  public void testBadFilterResults() {
    given()
        .accept("application/vnd.api+json")
        .header("data-partition", "main")
        .when()
        .queryParam("filter", "&^$*^%#$")
        .get(Paths.API_ENDPOINT)
        .then()
        .statusCode(400);
  }

  @Test
  public void testCreateWithoutBody() {
        given()
            .accept("application/vnd.api+json")
            .header("data-partition", "main")
            .contentType("application/vnd.api+json")
            .when()
            .body("{}")
            .post(Paths.API_ENDPOINT)
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
  public void testHealthPostItem() {
    given()
        .when()
        .get(Paths.HEALTH_ENDPOINT + "/POST")
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
