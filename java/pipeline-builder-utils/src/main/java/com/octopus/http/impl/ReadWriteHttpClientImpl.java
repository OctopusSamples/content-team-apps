package com.octopus.http.impl;

import static org.jboss.logging.Logger.Level.DEBUG;
import static org.jboss.logging.Logger.Level.ERROR;

import io.vavr.control.Try;
import java.util.List;
import lombok.NonNull;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;

/**
 * A HTTP client capable of making requests that modify resources.
 */
public class ReadWriteHttpClientImpl extends ReadOnlyHttpClientImpl {
  private static final Logger LOG = Logger.getLogger(ReadWriteHttpClientImpl.class.toString());

  /**
   * Performs a HTTP POST request.
   *
   * @param url The URL to access.
   * @return A Try monad that either contains the String of the requested resource, or an exception.
   */
  public Try<String> post(
      @NonNull final String url,
      @NonNull final String body) {
    LOG.log(DEBUG, "StringHttpClient.post(String, String, String)");
    LOG.log(DEBUG, "url: " + url);
    LOG.log(DEBUG, "body: " + body);

    return getClient()
        .of(httpClient -> postResponse(httpClient, url, body, List.of())
            .of(response -> EntityUtils.toString(checkSuccess(response, url).getEntity()))
            .get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP POST response body: " + c))
        .onFailure(e -> LOG.log(ERROR, "Exception message: " + e.toString()));
  }

  /**
   * Performs a HTTP POST with custom headers.
   *
   * @param url     The URL to access.
   * @param body    The request body.
   * @param headers The request headers.
   * @return A Try monad that either contains the String of the requested resource, or an exception.
   */
  public Try<String> post(
      @NonNull final String url,
      @NonNull final String body,
      @NonNull final List<Header> headers) {
    LOG.log(DEBUG, "StringHttpClient.post(String, String, List<Header>)");
    LOG.log(DEBUG, "url: " + url);
    LOG.log(DEBUG, "body: " + body);
    LOG.log(DEBUG, "headers: " + headers);

    return getClient()
        .of(httpClient -> postResponse(
            httpClient,
            url,
            body,
            headers)
            .of(response -> EntityUtils.toString(checkSuccess(response, url).getEntity()))
            .get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP POST response body: " + c))
        .onFailure(e -> LOG.log(ERROR, "Exception message: " + e.toString()));
  }

  /**
   * Performs a HTTP POST with custom headers.
   *
   * @param url      The URL to access.
   * @param body     The request body.
   * @param username The optional username.
   * @param password The optional password.
   * @return A Try monad that either contains the String of the requested resource, or an exception.
   */
  public Try<String> post(
      @NonNull final String url,
      @NonNull final String body,
      final String username,
      final String password) {
    LOG.log(DEBUG, "StringHttpClient.post(String, String, String, String, String)");
    LOG.log(DEBUG, "url: " + url);
    LOG.log(DEBUG, "body: " + body);
    LOG.log(DEBUG, "username: " + username);

    return getClient()
        .of(httpClient -> postResponse(
            httpClient,
            url,
            body,
            buildHeaders(username, password))
            .of(response -> EntityUtils.toString(checkSuccess(response, url).getEntity()))
            .get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP POST response body: " + c))
        .onFailure(e -> LOG.log(ERROR, "Exception message: " + e.toString()));
  }

  /**
   * Performs a HTTP POST request.
   *
   * @param url The URL to access.
   * @return A Try monad that either contains the String of the requested resource, or an exception.
   */
  public Try<String> put(
      @NonNull final String url,
      @NonNull final String body) {
    LOG.log(DEBUG, "StringHttpClient.put(String, String, String)");
    LOG.log(DEBUG, "url: " + url);
    LOG.log(DEBUG, "body: " + body);

    return getClient()
        .of(httpClient -> putResponse(httpClient, url, body, List.of())
            .of(response -> EntityUtils.toString(checkSuccess(response, url).getEntity()))
            .get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP PUT response body: " + c))
        .onFailure(e -> LOG.log(ERROR, "Exception message: " + e.toString()));
  }

  /**
   * Performs a HTTP PUT with custom headers.
   *
   * @param url     The URL to access.
   * @param body    The request body.
   * @param headers The request headers.
   * @return A Try monad that either contains the String of the requested resource, or an exception.
   */
  public Try<String> put(
      @NonNull final String url,
      @NonNull final String body,
      @NonNull final List<Header> headers) {
    LOG.log(DEBUG, "StringHttpClient.put(String, String, List<Header>)");
    LOG.log(DEBUG, "url: " + url);
    LOG.log(DEBUG, "body: " + body);
    LOG.log(DEBUG, "headers: " + headers);

    return getClient()
        .of(httpClient -> putResponse(
            httpClient,
            url,
            body,
            headers)
            .of(response -> EntityUtils.toString(checkSuccess(response, url).getEntity()))
            .get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP PUT response body: " + c))
        .onFailure(e -> LOG.log(ERROR, "Exception message: " + e.toString()));
  }

  /**
   * Performs a HTTP PUT with custom headers.
   *
   * @param url      The URL to access.
   * @param body     The request body.
   * @param username The optional username.
   * @param password The optional password.
   * @return A Try monad that either contains the String of the requested resource, or an exception.
   */
  public Try<String> put(
      @NonNull final String url,
      @NonNull final String body,
      final String username,
      final String password) {
    LOG.log(DEBUG, "StringHttpClient.put(String, String, String, String, String)");
    LOG.log(DEBUG, "url: " + url);
    LOG.log(DEBUG, "body: " + body);
    LOG.log(DEBUG, "username: " + username);

    return getClient()
        .of(httpClient -> postResponse(
            httpClient,
            url,
            body,
            buildHeaders(username, password))
            .of(response -> EntityUtils.toString(checkSuccess(response, url).getEntity()))
            .get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP PUT response body: " + c))
        .onFailure(e -> LOG.log(ERROR, "Exception message: " + e.toString()));
  }

  private Try.WithResources1<CloseableHttpResponse> postResponse(
      @NonNull final CloseableHttpClient httpClient,
      @NonNull final String path,
      @NonNull final String body,
      @NonNull final List<Header> headers) {
    return postRequest(path, body, headers)
        .map(r -> Try.withResources(() -> httpClient.execute(r)))
        .get();
  }

  private Try.WithResources1<CloseableHttpResponse> putResponse(
      @NonNull final CloseableHttpClient httpClient,
      @NonNull final String path,
      @NonNull final String body,
      @NonNull final List<Header> headers) {
    return putRequest(path, body, headers)
        .map(r -> Try.withResources(() -> httpClient.execute(r)))
        .get();
  }

  private Try<HttpRequestBase> postRequest(
      @NonNull final String path,
      @NonNull final String body,
      @NonNull final List<Header> headers) {
    return Try.of(() -> new HttpPost(path))
        .andThenTry(h -> h.setEntity(new StringEntity(body)))
        .andThen(h -> headers.forEach(h::addHeader))
        .map(h -> (HttpRequestBase) h);
  }

  private Try<HttpRequestBase> putRequest(
      @NonNull final String path,
      @NonNull final String body,
      @NonNull final List<Header> headers) {
    return Try.of(() -> new HttpPut(path))
        .andThenTry(h -> h.setEntity(new StringEntity(body)))
        .andThen(h -> headers.forEach(h::addHeader))
        .map(h -> (HttpRequestBase) h);
  }
}
