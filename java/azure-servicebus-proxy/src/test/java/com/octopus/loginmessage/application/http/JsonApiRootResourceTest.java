package com.octopus.loginmessage.application.http;

import static io.restassured.RestAssured.given;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.loginmessage.BaseTest;
import com.octopus.loginmessage.application.Paths;
import com.octopus.features.DisableSecurityFeature;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JsonApiRootResourceTest extends BaseTest {

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
  public void testHealthPostItem() {
    given()
        .when()
        .get(Paths.HEALTH_ENDPOINT + "/POST")
        .then()
        .statusCode(200);
  }
}
