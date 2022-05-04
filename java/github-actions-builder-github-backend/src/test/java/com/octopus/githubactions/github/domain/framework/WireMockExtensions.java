package com.octopus.githubactions.github.domain.framework;


import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import com.google.common.collect.ImmutableMap;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.Map;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.SneakyThrows;


import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * See the Quarkus documentation <a
 * href="https://quarkus.io/guides/rest-client#using-a-mock-http-server-for-tests">here</a>.
 */
public class WireMockExtensions implements QuarkusTestResourceLifecycleManager {

  private WireMockServer wireMockServer;

  @SneakyThrows
  @Override
  public Map<String, String> start() {
    final int freePort = getFreePort();
    wireMockServer = new WireMockServer(freePort);
    wireMockServer.start();

    wireMockServer.stubFor(get(urlEqualTo("/user/public_emails"))
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

    return ImmutableMap.<String, String>builder()
        .put("github.encryption", "12345678901234567890123456789012")
        .put("github.salt", "12345678901234567890123456789012")
        .put("cognito.client-id", "clientid")
        .put("cognito.client-secret", "secret")
        .put("quarkus.rest-client.\"com.octopus.githubactions.github.infrastructure.client.GitHubApi\".url",
            wireMockServer.baseUrl())
        .build();
  }

  @Override
  public void stop() {
    if (null != wireMockServer) {
      wireMockServer.stop();
    }
  }

  private int getFreePort() {
    try {
      try (final ServerSocket socket = new ServerSocket(0)) {
        return socket.getLocalPort();
      }
    } catch (final Exception ex) {
      return Options.DEFAULT_PORT;
    }
  }
}