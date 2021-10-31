package com.octopus.jenkinsclient;

public class JenkinsDetails {

  private String host;
  private Integer port;

  public JenkinsDetails(final String host, final Integer port) {
    this.setHost(host);
    this.setPort(port);
  }

  public String getHost() {
    return host;
  }

  public void setHost(final String host) {
    this.host = host;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(final Integer port) {
    this.port = port;
  }

  public String toString() {
    return "http://" + host + ":" + port;
  }
}
