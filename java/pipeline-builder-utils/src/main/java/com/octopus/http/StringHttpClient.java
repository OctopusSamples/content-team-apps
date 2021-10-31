package com.octopus.http;

import static org.jboss.logging.Logger.Level.DEBUG;

import io.vavr.control.Try;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;

/**
 * An implementation of HttpClient that returns the string content of any accessed file.
 */
public class StringHttpClient implements HttpClient {

  private static final Logger LOG = Logger.getLogger(StringHttpClient.class.toString());

  /**
   * Performs a HTTP GET request.
   *
   * @param url The URL to access.
   * @return A Try monad that either contains the String of the requested resource, or an exception.
   */
  public Try<String> get(@NonNull final String url) {
    LOG.log(DEBUG, "StringHttpClient.get(String)");
    LOG.log(DEBUG, "url: " + url);

    return getClient()
        .of(httpClient -> getResponse(httpClient, url, List.of())
            .of(response -> EntityUtils.toString(checkSuccess(response).getEntity()))
            .get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP GET response body: " + c))
        .onFailure(e -> LOG.log(DEBUG, "Exception message: " + e.toString()));
  }

  @Override
  public Try<String> get(
      @NonNull final String url,
      final String username,
      final String password) {
    LOG.log(DEBUG, "StringHttpClient.get(String, String, String)");
    LOG.log(DEBUG, "url: " + url);
    LOG.log(DEBUG, "username: " + username);

    return getClient()
        .of(httpClient -> getResponse(
            httpClient, url,
            buildHeaders(username, password))
            .of(response -> EntityUtils.toString(checkSuccess(response).getEntity()))
            .get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP GET response body: " + c))
        .onFailure(e -> LOG.log(DEBUG, "Exception message: " + e.toString()));
  }

  @Override
  public Try<String> get(
      @NonNull final String url,
      @NonNull final List<Header> headers) {
    LOG.log(DEBUG, "StringHttpClient.get(String, List<Header>)");
    LOG.log(DEBUG, "url: " + url);
    LOG.log(DEBUG, "headers: " + headers);

    return getClient()
        .of(httpClient -> getResponse(
            httpClient, url,
            headers)
            .of(response -> EntityUtils.toString(checkSuccess(response).getEntity()))
            .get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP GET response body: " + c))
        .onFailure(e -> LOG.log(DEBUG, "Exception message: " + e.toString()));
  }

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
            .of(response -> EntityUtils.toString(checkSuccess(response).getEntity()))
            .get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP POST response body: " + c))
        .onFailure(e -> LOG.log(DEBUG, "Exception message: " + e.toString()));
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
            .of(response -> EntityUtils.toString(checkSuccess(response).getEntity()))
            .get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP POST response body: " + c))
        .onFailure(e -> LOG.log(DEBUG, "Exception message: " + e.toString()));
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
            .of(response -> EntityUtils.toString(checkSuccess(response).getEntity()))
            .get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP POST response body: " + c))
        .onFailure(e -> LOG.log(DEBUG, "Exception message: " + e.toString()));
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
            .of(response -> EntityUtils.toString(checkSuccess(response).getEntity()))
            .get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP PUT response body: " + c))
        .onFailure(e -> LOG.log(DEBUG, "Exception message: " + e.toString()));
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
            .of(response -> EntityUtils.toString(checkSuccess(response).getEntity()))
            .get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP PUT response body: " + c))
        .onFailure(e -> LOG.log(DEBUG, "Exception message: " + e.toString()));
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
            .of(response -> EntityUtils.toString(checkSuccess(response).getEntity()))
            .get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP PUT response body: " + c))
        .onFailure(e -> LOG.log(DEBUG, "Exception message: " + e.toString()));
  }

  /**
   * Performs a HTTP HEAD request.
   *
   * @param url The URL to access.
   * @return true if the request succeeded, and false otherwise.
   */
  public boolean head(@NonNull final String url) {
    LOG.log(DEBUG, "StringHttpClient.head(String)");
    LOG.log(DEBUG, "url: " + url);

    return getClient()
        .of(httpClient -> headResponse(httpClient, url, List.of()).of(this::checkSuccess).get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP HEAD request was successful."))
        .onFailure(e -> LOG.log(DEBUG, "Exception message: " + e.toString()))
        .isSuccess();
  }

  @Override
  public boolean head(
      @NonNull final String url,
      final String username,
      final String password) {
    LOG.log(DEBUG, "StringHttpClient.head(String, String, String)");
    LOG.log(DEBUG, "url: " + url);
    LOG.log(DEBUG, "username: " + username);

    return getClient()
        .of(httpClient -> headResponse(
            httpClient, url,
            buildHeaders(username, password))
            .of(this::checkSuccess).get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP HEAD request was successful."))
        .onFailure(e -> LOG.log(DEBUG, "Exception message: " + e.toString()))
        .isSuccess();
  }

  @Override
  public boolean head(String url, List<Header> headers) {
    LOG.log(DEBUG, "StringHttpClient.head(String, List<Header>)");
    LOG.log(DEBUG, "url: " + url);
    LOG.log(DEBUG, "headers: " + headers);

    return getClient()
        .of(httpClient -> headResponse(
            httpClient, url,
            headers)
            .of(this::checkSuccess).get())
        .onSuccess(c -> LOG.log(DEBUG, "HTTP HEAD request was successful."))
        .onFailure(e -> LOG.log(DEBUG, "Exception message: " + e.toString()))
        .isSuccess();
  }

  private List<Header> buildHeaders(final String username, final String password) {
    return Stream.of(buildAuthHeader(username, password))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  private Optional<BasicHeader> buildAuthHeader(final String username, final String password) {
    if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
      return Optional.empty();
    }

    return Optional.of(new BasicHeader(
        "AUTHORIZATION",
        "Basic " + Base64.encodeBase64((username + ":" + password).getBytes())));
  }

  private Try.WithResources1<CloseableHttpClient> getClient() {
    return Try.withResources(HttpClients::createDefault);
  }

  private Try.WithResources1<CloseableHttpResponse> getResponse(
      @NonNull final CloseableHttpClient httpClient,
      @NonNull final String path,
      @NonNull final List<Header> headers) {
    return Try.withResources(() -> httpClient.execute(getRequest(path, headers)));
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

  private Try.WithResources1<CloseableHttpResponse> headResponse(
      @NonNull final CloseableHttpClient httpClient,
      @NonNull final String path,
      @NonNull final List<Header> headers) {
    return Try.withResources(() -> httpClient.execute(headRequest(path, headers)));
  }

  private HttpRequestBase headRequest(
      @NonNull final String path,
      @NonNull final List<Header> headers) {
    final HttpRequestBase request = new HttpHead(path);
    headers.forEach(request::addHeader);
    return request;
  }

  private HttpRequestBase getRequest(
      @NonNull final String path,
      @NonNull final List<Header> headers) {
    final HttpRequestBase request = new HttpGet(path);
    headers.forEach(request::addHeader);
    return request;
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

  private CloseableHttpResponse checkSuccess(@NonNull final CloseableHttpResponse response)
      throws Exception {
    LOG.log(DEBUG, "StringHttpClient.checkSuccess(CloseableHttpResponse)");

    final int code = response.getStatusLine().getStatusCode();
    if (code >= 200 && code <= 399) {
      LOG.log(DEBUG, "Response code " + code + " indicated success");
      return response;
    }

    LOG.log(DEBUG, "Response code " + code + " did not indicate success");
    throw new Exception("Response did not indicate success");
  }
}
