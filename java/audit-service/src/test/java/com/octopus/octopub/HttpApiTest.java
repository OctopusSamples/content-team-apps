package com.octopus.octopub;

import static io.restassured.RestAssured.given;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.octopub.domain.entities.Audit;
import com.octopus.octopub.infrastructure.utilities.LiquidbaseUpdater;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ValidatableResponse;
import java.sql.SQLException;
import java.util.Objects;
import javax.inject.Inject;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpApiTest extends BaseTest {

  @Inject LiquidbaseUpdater liquidbaseUpdater;

  @Inject ResourceConverter resourceConverter;

  @BeforeAll
  public void setup() throws SQLException, LiquibaseException {
    liquidbaseUpdater.update();
  }

  @Test
  public void testCreateAndGetAudit() throws DocumentSerializationException {
    final ValidatableResponse response =
        given()
            .accept("application/vnd.api+json,application/vnd.api+json; dataPartition=main")
            .contentType("application/vnd.api+json")
            .when()
            .body(
                auditToResourceDocument(
                    resourceConverter, createAudit("testCreateAndGetResource")))
            .post("/api/audits")
            .then()
            .statusCode(200)
            .body(
                new LambdaMatcher(
                    a -> getAuditFromDocument(resourceConverter, a.toString()) != null,
                    "Resource should be returned"));

    final Audit created =
        getAuditFromDocument(resourceConverter, response.extract().body().asString());

    given()
        .accept("application/vnd.api+json,application/vnd.api+json; dataPartition=main")
        .when()
        .get("/api/audits")
        .then()
        .statusCode(200)
        .body(
            new LambdaMatcher(
                a ->
                    getAuditsFromDocument(resourceConverter, a.toString()).stream()
                        .anyMatch(p -> Objects.equals(created.getId(), p.getId())),
                "Resource should be returned"));

    given()
        .accept("application/vnd.api+json,application/vnd.api+json; dataPartition=main")
        .when()
        .get("/api/audits/" + created.getId())
        .then()
        .statusCode(200)
        .body(
            new LambdaMatcher(
                a ->
                    getAuditFromDocument(resourceConverter, a.toString())
                        .getSubject()
                        .equals(created.getSubject()),
                "Resource should be returned"));
  }

  @Test
  public void failToGetResourceInDifferentPartition() throws DocumentSerializationException {
    final ValidatableResponse response =
        given()
            .accept("application/vnd.api+json,application/vnd.api+json; dataPartition=testing2")
            .contentType("application/vnd.api+json")
            .when()
            .body(
                auditToResourceDocument(
                    resourceConverter, createAudit("testCreateAndGetResource")))
            .post("/api/audits")
            .then()
            .statusCode(200);

    final Audit created =
        getAuditFromDocument(resourceConverter, response.extract().body().asString());

    given()
        .accept("application/vnd.api+json, application/vnd.api+json; dataPartition=testing")
        .when()
        .get("/api/audits/" + created.getId())
        .then()
        .statusCode(404);
  }

  @Test
  public void failWithMissingContentTypeForPost() throws DocumentSerializationException {
    final ValidatableResponse response =
        given()
            .accept("application/vnd.api+json,application/vnd.api+json; dataPartition=main")
            .when()
            .body(
                auditToResourceDocument(
                    resourceConverter, createAudit("testCreateAndGetResource")))
            .post("/api/audits")
            .then()
            .statusCode(415);
  }

  @Test
  public void failWithoutPlainAcceptForPost() throws DocumentSerializationException {
    final ValidatableResponse response =
        given()
            .accept("application/vnd.api+json; dataPartition=main")
            .contentType("application/vnd.api+json")
            .when()
            .body(
                auditToResourceDocument(
                    resourceConverter, createAudit("testCreateAndGetResource")))
            .post("/api/audits")
            .then()
            .statusCode(406);
  }

  @Test
  public void failWithoutPlainAcceptForGet() {
    final ValidatableResponse response =
        given()
            .accept("application/vnd.api+json; dataPartition=main")
            .when()
            .get("/api/audits/1")
            .then()
            .statusCode(406);
  }

  @Test
  public void failWithoutPlainAcceptForGetAll() {
    final ValidatableResponse response =
        given()
            .accept("application/vnd.api+json; dataPartition=main")
            .when()
            .get("/api/audits")
            .then()
            .statusCode(406);
  }

  @Test
  public void testFilterResults() throws DocumentSerializationException {
    final ValidatableResponse response =
        given()
            .accept("application/vnd.api+json,application/vnd.api+json; dataPartition=main")
            .contentType("application/vnd.api+json")
            .when()
            .body(
                auditToResourceDocument(
                    resourceConverter, createAudit("testCreateAndGetResource")))
            .post("/api/audits")
            .then()
            .statusCode(200)
            .body(
                new LambdaMatcher(
                    a -> getAuditFromDocument(resourceConverter, a.toString()) != null,
                    "Resource should be returned"));

    final Audit created =
        getAuditFromDocument(resourceConverter, response.extract().body().asString());

    given()
        .accept("application/vnd.api+json,application/vnd.api+json; dataPartition=main")
        .when()
        .queryParam("filter", "id==" + created.getId())
        .get("/api/audits")
        .then()
        .statusCode(200)
        .body(
            new LambdaMatcher(
                a ->
                    getAuditsFromDocument(resourceConverter, a.toString()).stream()
                        .anyMatch(p -> Objects.equals(created.getId(), p.getId())),
                "Resource should be returned"));

    given()
        .accept("application/vnd.api+json,application/vnd.api+json; dataPartition=main")
        .when()
        .queryParam("filter", "subject==testCreateAndGetResource")
        .get("/api/audits")
        .then()
        .statusCode(200)
        .body(
            new LambdaMatcher(
                a ->
                    getAuditsFromDocument(resourceConverter, a.toString()).stream()
                        .anyMatch(p -> Objects.equals(created.getId(), p.getId())),
                "Resource should be returned"));

    given()
        .accept("application/vnd.api+json,application/vnd.api+json; dataPartition=main")
        .when()
        .queryParam("filter", "subject!=blah")
        .get("/api/audits")
        .then()
        .statusCode(200)
        .body(
            new LambdaMatcher(
                a ->
                    getAuditsFromDocument(resourceConverter, a.toString()).stream()
                        .anyMatch(p -> Objects.equals(created.getId(), p.getId())),
                "Resource should be returned"));

    given()
        .accept("application/vnd.api+json,application/vnd.api+json; dataPartition=main")
        .when()
        .queryParam("filter", "subject==test*")
        .get("/api/audits")
        .then()
        .statusCode(200)
        .body(
            new LambdaMatcher(
                a ->
                    getAuditsFromDocument(resourceConverter, a.toString()).stream()
                        .anyMatch(p -> Objects.equals(created.getId(), p.getId())),
                "Resource should be returned"));

    given()
        .accept("application/vnd.api+json,application/vnd.api+json; dataPartition=main")
        .when()
        .queryParam("filter", "subject=in=(testCreateAndGetResource)")
        .get("/api/audits")
        .then()
        .statusCode(200)
        .body(
            new LambdaMatcher(
                a ->
                    getAuditsFromDocument(resourceConverter, a.toString()).stream()
                        .anyMatch(p -> Objects.equals(created.getId(), p.getId())),
                "Resource should be returned"));

    given()
        .accept("application/vnd.api+json,application/vnd.api+json; dataPartition=main")
        .when()
        .queryParam("filter", "id<" + (created.getId() + 1))
        .get("/api/audits")
        .then()
        .statusCode(200)
        .body(
            new LambdaMatcher(
                a ->
                    getAuditsFromDocument(resourceConverter, a.toString()).stream()
                        .anyMatch(p -> Objects.equals(created.getId(), p.getId())),
                "Resource should be returned"));
  }

  @Test
  public void testBadFilterResults() {
    given()
        .accept("application/vnd.api+json,application/vnd.api+json; dataPartition=main")
        .when()
        .queryParam("filter", "&^$*^%#$")
        .get("/api/audits")
        .then()
        .statusCode(400);
  }

  @Test
  public void testCreateWithoutBody() {
        given()
            .accept("application/vnd.api+json,application/vnd.api+json; dataPartition=main")
            .contentType("application/vnd.api+json")
            .when()
            .body("{}")
            .post("/api/audits")
            .then()
            .statusCode(400);
  }
}
