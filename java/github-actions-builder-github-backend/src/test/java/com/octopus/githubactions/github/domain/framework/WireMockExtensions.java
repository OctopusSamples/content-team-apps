package com.octopus.githubactions.github.domain.framework;


import java.util.Collections;
import java.util.Map;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * https://quarkus.io/guides/rest-client#using-a-mock-http-server-for-tests
 */
import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class WireMockExtensions implements QuarkusTestResourceLifecycleManager {

  private WireMockServer wireMockServer;

  @Override
  public Map<String, String> start() {
    wireMockServer = new WireMockServer();
    wireMockServer.start();

    stubFor(get(urlEqualTo("/user/public_emails"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
                "[\n"
                    + "    {\n"
                    + "        \"email\": \"matthewcasperson@example.org\",\n"
                    + "        \"primary\": true,\n"
                    + "        \"verified\": true,\n"
                    + "        \"visibility\": \"public\"\n"
                    + "    }\n"
                    + "]"
            )));

    return Collections.singletonMap(
        "quarkus.rest-client.\"com.octopus.githubactions.github.infrastructure.client.GitHubUser\".url",
        wireMockServer.baseUrl());
  }

  @Override
  public void stop() {
    if (null != wireMockServer) {
      wireMockServer.stop();
    }
  }
}