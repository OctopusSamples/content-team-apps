package com.octopus.jenkinsclient;

import io.vavr.control.Try;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.NonNull;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class JenkinsClient {

  public Try<String> waitServerStarted(final JenkinsDetails jenkinsDetails) {
    for (int i = 0; i < 12; ++i) {
      final Try<String> serverStarted = getClient()
          .of(httpClient -> postResponse(httpClient, jenkinsDetails.toString() + "/login")
              .of(response -> EntityUtils.toString(checkSuccess(response).getEntity())))
          .get();

      if (serverStarted.isSuccess()) {
        return serverStarted;
      }
      Try.run(() -> Thread.sleep(5000));
    }

    return Try.failure(new Exception("Failed to wait for server to start"));
  }

  public CloseableHttpResponse checkSuccess(@NonNull final CloseableHttpResponse response)
      throws Exception {

    final int code = response.getStatusLine().getStatusCode();
    if (code >= 200 && code <= 399) {
      return response;
    }

    throw new Exception("Response did not indicate success");
  }

  public Try<Document> waitJobBuilding(final JenkinsDetails jenkinsDetails, final String name) {
    for (int i = 0; i < 240; ++i) {
      final Try<Document> building = getClient()
          .of(httpClient -> postResponse(httpClient,
              jenkinsDetails.toString() + "/job/" + name + "/1/api/xml?depth=0")
              .of(response -> EntityUtils.toString(response.getEntity()))
              .mapTry(this::parseXML)
              .get());
      if (building.isSuccess() && !isBuilding(building.get())) {
        return building;
      }
      Try.run(() -> Thread.sleep(5000));
    }

    return Try.failure(new Exception("Failed while waiting for build to complete"));
  }

  public Try<String> getJobLogs(final String hostname, final Integer port, final String name) {
    return getClient()
        .of(httpClient -> postResponse(httpClient,
            "http://" + hostname + ":" + port + "/job/" + name + "/1/consoleText")
            .of(response -> EntityUtils.toString(response.getEntity()))
            .get());
  }

  private boolean isBuilding(final Document doc) {
    return doc.getDocumentElement()
        .getElementsByTagName("building")
        .item(0)
        .getTextContent()
        .equals("true");
  }

  public boolean isSuccess(final Document doc) {
    final String result = doc.getDocumentElement()
        .getElementsByTagName("result")
        .item(0)
        .getTextContent();
    return result.equals("SUCCESS") || result.equals("UNSTABLE");
  }

  private Document parseXML(final String xml)
      throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    return dBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
  }

  public Try<String> startJob(final JenkinsDetails jenkinsDetails, final String name) {
    return getClient()
        .of(httpClient -> postResponse(httpClient,
            jenkinsDetails.toString() + "/job/" + name + "/build")
            .of(response -> EntityUtils.toString(response.getEntity()))
            .get());
  }

  public Try<String> restartJenkins(final JenkinsDetails jenkinsDetails) {
    return getClient()
        .of(httpClient -> postResponse(httpClient, jenkinsDetails.toString() + "/reload")
            .of(response -> EntityUtils.toString(response.getEntity()))
            .get());
  }

  private Try.WithResources1<CloseableHttpClient> getClient() {
    return Try.withResources(HttpClients::createDefault);
  }

  private Try.WithResources1<CloseableHttpResponse> postResponse(
      @NonNull final CloseableHttpClient httpClient,
      @NonNull final String path) {
    return Try.withResources(() -> httpClient.execute(new HttpPost(path)));
  }
}
