package com.octopus;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class OctopusMetricsServerTest {

    @Test
    public void testStatusEndpoint() {
        given()
          .when().get("/apis/custom.metrics.k8s.io/v1beta1")
          .then()
             .statusCode(200)
             .body(is("{\"status\": \"healthy\"}"));
    }

}