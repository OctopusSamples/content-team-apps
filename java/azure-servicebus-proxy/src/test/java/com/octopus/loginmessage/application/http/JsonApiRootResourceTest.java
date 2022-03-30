package com.octopus.loginmessage.application.http;

import static io.restassured.RestAssured.given;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.loginmessage.BaseTest;
import com.octopus.loginmessage.CommercialAzureServiceBusTestProfile;
import com.octopus.loginmessage.application.TestPaths;
import com.octopus.features.DisableSecurityFeature;
import com.octopus.loginmessage.domain.entities.GithubUserLoggedInForFreeToolsEventV1;
import com.octopus.loginmessage.domain.framework.producers.JsonApiConverter;
import com.octopus.loginmessage.infrastructure.octofront.CommercialServiceBus;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(CommercialAzureServiceBusTestProfile.class)
public class JsonApiRootResourceTest extends BaseTest {

  @Inject ResourceConverter resourceConverter;

  @InjectMock
  DisableSecurityFeature cognitoDisableAuth;

  @InjectMock
  CommercialServiceBus commercialServiceBus;

  @Inject
  JsonApiConverter jsonApiConverter;

  @BeforeEach
  public void beforeEach() {
    Mockito.when(cognitoDisableAuth.getCognitoAuthDisabled()).thenReturn(true);
  }

  @Test
  public void testCreate() throws DocumentSerializationException {
    given()
        .accept("application/vnd.api+json")
        .header("data-partition", "main")
        .contentType("application/vnd.api+json")
        .when()
        .body(new String(jsonApiConverter.buildResourceConverter().writeDocument(
            new JSONAPIDocument<>(GithubUserLoggedInForFreeToolsEventV1
                .builder()
                .emailAddress("test@test.com")
                .build()))))
        .post(TestPaths.API_ENDPOINT)
        .then()
        .statusCode(202);
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
            .post(TestPaths.API_ENDPOINT)
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
            .post(TestPaths.API_ENDPOINT)
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
            .post(TestPaths.API_ENDPOINT)
            .then()
            .statusCode(400);
  }

  @Test
  public void testHealthPostItem() {
    given()
        .when()
        .get(TestPaths.HEALTH_ENDPOINT + "/POST")
        .then()
        .statusCode(200);
  }
}
