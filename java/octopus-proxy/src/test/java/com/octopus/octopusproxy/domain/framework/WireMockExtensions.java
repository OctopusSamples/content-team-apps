package com.octopus.octopusproxy.domain.framework;


import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import com.google.common.collect.ImmutableMap;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.net.ServerSocket;
import java.util.Map;
import lombok.SneakyThrows;

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

    wireMockServer.stubFor(get(urlEqualTo("/api/spaces?partialName=ECS+mcasperson"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("{\n"
                + "    \"ItemType\": \"Space\",\n"
                + "    \"TotalResults\": 3,\n"
                + "    \"ItemsPerPage\": 30,\n"
                + "    \"NumberOfPages\": 1,\n"
                + "    \"LastPageNumber\": 0,\n"
                + "    \"Items\": [\n"
                + "        {\n"
                + "            \"Id\": \"Spaces-923\",\n"
                + "            \"Name\": \"ECS mcasperson\",\n"
                + "            \"Description\": \"A space created by app builder. This resource is created and managed by the [Octopus Terraform provider](https://registry.terraform.io/providers/OctopusDeployLabs/octopusdeploy/latest/docs). The Terraform files can be found in the [GitHub repo](https://github.com/mcasperson/AppBuilder-ECS).\",\n"
                + "            \"IsDefault\": false,\n"
                + "            \"IsPrivate\": false,\n"
                + "            \"PrivateSpaceOwner\": null,\n"
                + "            \"TaskQueueStopped\": false,\n"
                + "            \"SpaceManagersTeams\": [\n"
                + "                \"teams-spacemanagers-Spaces-923\"\n"
                + "            ],\n"
                + "            \"SpaceManagersTeamMembers\": [],\n"
                + "            \"Icon\": null,\n"
                + "            \"ExtensionSettings\": [],\n"
                + "            \"LastModifiedOn\": \"0001-01-01T00:00:00.000+00:00\",\n"
                + "            \"Links\": {\n"
                + "                \"Self\": \"/api/spaces/Spaces-923\",\n"
                + "                \"SpaceHome\": \"/api/Spaces-923\",\n"
                + "                \"Web\": \"/app#/spaces/Spaces-923\",\n"
                + "                \"Logo\": \"/api/spaces/Spaces-923/logo?cb=2022.2.5531\",\n"
                + "                \"Search\": \"/api/spaces/Spaces-923/search\"\n"
                + "            }\n"
                + "        }\n"
                + "    ],\n"
                + "    \"Links\": {\n"
                + "        \"Self\": \"/api/spaces?skip=0&take=30&partialName=ECS\",\n"
                + "        \"Template\": \"/api/spaces{?skip,take,ids,partialName}\",\n"
                + "        \"Page.All\": \"/api/spaces?skip=0&take=2147483647&partialName=ECS\",\n"
                + "        \"Page.Current\": \"/api/spaces?skip=0&take=30&partialName=ECS\",\n"
                + "        \"Page.Last\": \"/api/spaces?skip=0&take=30&partialName=ECS\"\n"
                + "    }\n"
                + "}"
            )));

    wireMockServer.stubFor(get(urlEqualTo("/api/spaces?partialName=ECS+octo-matt"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("{\n"
                + "    \"ItemType\": \"Space\",\n"
                + "    \"TotalResults\": 3,\n"
                + "    \"ItemsPerPage\": 30,\n"
                + "    \"NumberOfPages\": 1,\n"
                + "    \"LastPageNumber\": 0,\n"
                + "    \"Items\": [\n"
                + "        {\n"
                + "            \"Id\": \"Spaces-923\",\n"
                + "            \"Name\": \"ECS octo-matt\",\n"
                + "            \"Description\": \"A space created by app builder. This resource is created and managed by the [Octopus Terraform provider](https://registry.terraform.io/providers/OctopusDeployLabs/octopusdeploy/latest/docs). The Terraform files can be found in the [GitHub repo](https://github.com/mcasperson/AppBuilder-ECS).\",\n"
                + "            \"IsDefault\": false,\n"
                + "            \"IsPrivate\": false,\n"
                + "            \"PrivateSpaceOwner\": null,\n"
                + "            \"TaskQueueStopped\": false,\n"
                + "            \"SpaceManagersTeams\": [\n"
                + "                \"teams-spacemanagers-Spaces-923\"\n"
                + "            ],\n"
                + "            \"SpaceManagersTeamMembers\": [],\n"
                + "            \"Icon\": null,\n"
                + "            \"ExtensionSettings\": [],\n"
                + "            \"LastModifiedOn\": \"0001-01-01T00:00:00.000+00:00\",\n"
                + "            \"Links\": {\n"
                + "                \"Self\": \"/api/spaces/Spaces-923\",\n"
                + "                \"SpaceHome\": \"/api/Spaces-923\",\n"
                + "                \"Web\": \"/app#/spaces/Spaces-923\",\n"
                + "                \"Logo\": \"/api/spaces/Spaces-923/logo?cb=2022.2.5531\",\n"
                + "                \"Search\": \"/api/spaces/Spaces-923/search\"\n"
                + "            }\n"
                + "        }\n"
                + "    ],\n"
                + "    \"Links\": {\n"
                + "        \"Self\": \"/api/spaces?skip=0&take=30&partialName=ECS\",\n"
                + "        \"Template\": \"/api/spaces{?skip,take,ids,partialName}\",\n"
                + "        \"Page.All\": \"/api/spaces?skip=0&take=2147483647&partialName=ECS\",\n"
                + "        \"Page.Current\": \"/api/spaces?skip=0&take=30&partialName=ECS\",\n"
                + "        \"Page.Last\": \"/api/spaces?skip=0&take=30&partialName=ECS\"\n"
                + "    }\n"
                + "}"
            )));

    wireMockServer.stubFor(get(urlEqualTo("/api/spaces?partialName=unauthorized"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(403)
            .withBody("{\n"
                + "    \"ErrorMessage\": \"You do not have permission to perform this action. Please contact your Octopus administrator. You have to have either SpaceView permission or access to the data within at least one space.\"\n"
                + "}"
            )));

    wireMockServer.stubFor(get(urlEqualTo("/api/spaces?partialName=missing"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(404)
            .withBody("{\n"
                + "    \"ErrorMessage\": \"The resource you requested was not found.\"\n"
                + "}"
            )));

    wireMockServer.stubFor(get(urlEqualTo("/api/spaces?partialName=bad"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(500)
            .withBody("{}"
            )));

    wireMockServer.stubFor(get(urlEqualTo("/api/spaces?partialName=corrupt"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody("thisisnotjson"
            )));

    wireMockServer.stubFor(get(urlEqualTo("/api/Spaces/Spaces-1"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody("{\n"
                + "    \"Id\": \"Spaces-1\",\n"
                + "    \"Name\": \"Default\",\n"
                + "    \"Description\": null,\n"
                + "    \"IsDefault\": false,\n"
                + "    \"IsPrivate\": false,\n"
                + "    \"PrivateSpaceOwner\": null,\n"
                + "    \"TaskQueueStopped\": false,\n"
                + "    \"SpaceManagersTeams\": [\n"
                + "        \"teams-spacemanagers-Spaces-1\",\n"
                + "        \"Teams-41\"\n"
                + "    ],\n"
                + "    \"SpaceManagersTeamMembers\": [\n"
                + "        \"Users-141\",\n"
                + "        \"Users-21\"\n"
                + "    ],\n"
                + "    \"Icon\": null,\n"
                + "    \"ExtensionSettings\": [],\n"
                + "    \"LastModifiedOn\": \"0001-01-01T00:00:00.000+00:00\",\n"
                + "    \"Links\": {\n"
                + "        \"Self\": \"/api/spaces/Spaces-742\",\n"
                + "        \"SpaceHome\": \"/api/Spaces-742\",\n"
                + "        \"Web\": \"/app#/spaces/Spaces-742\",\n"
                + "        \"Logo\": \"/api/spaces/Spaces-742/logo?cb=2022.2.5531\",\n"
                + "        \"Search\": \"/api/spaces/Spaces-742/search\"\n"
                + "    }\n"
                + "}"
            )));

    wireMockServer.stubFor(get(urlEqualTo("/api/Spaces/Spaces-2"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(404)
            .withBody("{\n"
                + "    \"ErrorMessage\": \"The resource 'Spaces-1' was not found.\"\n"
                + "}"
            )));

    return ImmutableMap.<String, String>builder()
        .put("wiremock.url", wireMockServer.baseUrl())
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