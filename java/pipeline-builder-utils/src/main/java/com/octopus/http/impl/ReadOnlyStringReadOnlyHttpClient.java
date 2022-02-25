package com.octopus.http.impl;

import static org.jboss.logging.Logger.Level.DEBUG;
import static org.jboss.logging.Logger.Level.ERROR;

import com.octopus.http.ReadOnlyHttpClient;
import io.vavr.control.Try;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;

/**
 * A readonly HTTP client. This is important, because GitHub does not offer a readonly scope when
 * accessing repos: https://github.com/github/feedback/discussions/7891. So by ensuring we use a
 * client that can only make GET or HEAD calls, we can be sure we don't make any unwanted
 * modifications. All GET and HEAD requests are cached to help prevent GitHub API rate limit
 * issues.
 */
public class ReadOnlyStringReadOnlyHttpClient implements ReadOnlyHttpClient {

  private static final Logger LOG = Logger.getLogger(
      ReadOnlyStringReadOnlyHttpClient.class.toString());
  private static final Map<String, Boolean> CACHED_BOOLEAN_RESULTS = new ConcurrentHashMap<>();
  private static final Map<String, Try<String>> CACHED_STRING_RESULTS = new ConcurrentHashMap<>();

  /**
   * Performs a HTTP GET request.
   *
   * @param url The URL to access.
   * @return A Try monad that either contains the String of the requested resource, or an exception.
   */
  public Try<String> get(@NonNull final String url) {
    LOG.log(DEBUG, "StringHttpClient.get(String)");
    LOG.log(DEBUG, "url: " + url);

    return CACHED_STRING_RESULTS.computeIfAbsent(
        generateCacheKey("GET", url, null),
        s ->
            getClient()
                .of(httpClient -> getResponse(httpClient, url, List.of())
                    .of(response -> EntityUtils.toString(checkSuccess(response).getEntity()))
                    .get())
                .onSuccess(c -> LOG.log(DEBUG, "HTTP GET response body: " + c))
                .onFailure(e -> LOG.log(DEBUG, "Exception message: " + e.toString())));
  }

  @Override
  public Try<String> get(
      @NonNull final String url,
      final String username,
      final String password,
      final String accessToken) {
    LOG.log(DEBUG, "StringHttpClient.get(String, String, String)");
    LOG.log(DEBUG, "url: " + url);
    LOG.log(DEBUG, "username: " + username);
    LOG.log(DEBUG, "password present: " + !StringUtils.isBlank(password));
    LOG.log(DEBUG, "accessToken present: " + !StringUtils.isBlank(accessToken));

    /*
      The authentication schemes for GitHub must account for a variety of situations.

      When accessing the API unauthenticated, we can use the credentials of a GitHub application
      to ensure we get the higher rate limits.

      When accessing the API authenticated, we pass the access token of the user to gain access
      to any private resources.

      User auth (defined in the accessToken) takes priority.
     */

    final List<Header> headers = StringUtils.isNotBlank(accessToken)
        ? List.of(new BasicHeader("Authorization", "token " + accessToken))
        : buildHeaders(username, password);

    return CACHED_STRING_RESULTS.computeIfAbsent(
        generateCacheKey("GET", url, headers),
        s ->
            getClient()
                .of(httpClient -> getResponse(
                    httpClient,
                    url,
                    headers)
                    .of(response -> EntityUtils.toString(checkSuccess(response).getEntity()))
                    .get())
                .onSuccess(c -> LOG.log(DEBUG, "HTTP GET response body: " + c))
                .onFailure(e -> LOG.log(ERROR, "Exception message: " + e.toString())));
  }

  @Override
  public Try<String> get(
      @NonNull final String url,
      @NonNull final List<Header> headers) {
    LOG.log(DEBUG, "StringHttpClient.get(String, List<Header>)");
    LOG.log(DEBUG, "url: " + url);
    LOG.log(DEBUG, "headers: " + headers);

    return CACHED_STRING_RESULTS.computeIfAbsent(
        generateCacheKey("GET", url, headers),
        s ->
            getClient()
                .of(httpClient -> getResponse(
                    httpClient, url,
                    headers)
                    .of(response -> EntityUtils.toString(checkSuccess(response).getEntity()))
                    .get())
                .onSuccess(c -> LOG.log(DEBUG, "HTTP GET response body: " + c))
                .onFailure(e -> LOG.log(ERROR, "Exception message: " + e.toString())));
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

    return CACHED_BOOLEAN_RESULTS.computeIfAbsent(
        generateCacheKey("HEAD", url, null),
        s ->
            getClient()
                .of(httpClient -> headResponse(httpClient, url, List.of()).of(this::checkSuccess)
                    .get())
                .onSuccess(c -> LOG.log(DEBUG, "HTTP HEAD request was successful."))
                .onFailure(e -> LOG.log(ERROR, "Exception message: " + e.toString()))
                .isSuccess());
  }

  @Override
  public boolean head(String url, String username, String password, String accessToken) {
    LOG.log(DEBUG, "StringHttpClient.head(String, String, String)");
    LOG.log(DEBUG, "url: " + url);
    LOG.log(DEBUG, "username: " + username);
    LOG.log(DEBUG, "password present: " + !StringUtils.isBlank(password));
    LOG.log(DEBUG, "accessToken present: " + !StringUtils.isBlank(accessToken));

    final List<Header> headers = StringUtils.isNotBlank(accessToken)
        ? List.of(new BasicHeader("Authorization", "token " + accessToken))
        : buildHeaders(username, password);

    return CACHED_BOOLEAN_RESULTS.computeIfAbsent(
        generateCacheKey("HEAD", url, headers),
        s ->
            getClient()
                .of(httpClient -> headResponse(
                    httpClient,
                    url,
                    headers)
                    .of(this::checkSuccess)
                    .get())
                .isSuccess()
    );
  }

  @Override
  public boolean head(
      @NonNull final String url,
      final String username,
      final String password) {
    LOG.log(DEBUG, "StringHttpClient.head(String, String, String)");
    LOG.log(DEBUG, "url: " + url);
    LOG.log(DEBUG, "username: " + username);

    return CACHED_BOOLEAN_RESULTS.computeIfAbsent(
        generateCacheKey("HEAD", url, null),
        s ->
            getClient()
                .of(httpClient -> headResponse(
                    httpClient, url,
                    buildHeaders(username, password))
                    .of(this::checkSuccess).get())
                .onSuccess(c -> LOG.log(DEBUG, "HTTP HEAD request was successful."))
                .onFailure(e -> LOG.log(ERROR, "Exception message: " + e.toString()))
                .isSuccess());
  }

  @Override
  public boolean head(String url, List<Header> headers) {
    LOG.log(DEBUG, "StringHttpClient.head(String, List<Header>)");
    LOG.log(DEBUG, "url: " + url);
    LOG.log(DEBUG, "headers: " + headers);

    return CACHED_BOOLEAN_RESULTS.computeIfAbsent(
        generateCacheKey("HEAD", url, headers),
        s ->
            getClient()
                .of(httpClient -> headResponse(
                    httpClient, url,
                    headers)
                    .of(this::checkSuccess).get())
                .onSuccess(c -> LOG.log(DEBUG, "HTTP HEAD request was successful."))
                .onFailure(e -> LOG.log(ERROR, "Exception message: " + e.toString()))
                .isSuccess());
  }

  protected List<Header> buildHeaders(final String username, final String password) {
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

  protected Try.WithResources1<CloseableHttpClient> getClient() {
    return Try.withResources(HttpClients::createDefault);
  }

  private Try.WithResources1<CloseableHttpResponse> getResponse(
      @NonNull final CloseableHttpClient httpClient,
      @NonNull final String path,
      @NonNull final List<Header> headers) {
    return Try.withResources(() -> httpClient.execute(getRequest(path, headers)));
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

  private String generateCacheKey(@NonNull final String method, @NonNull final String url,
      final List<Header> headers) {
    final StringBuilder sb = new StringBuilder(method + "\n" + url);
    if (headers != null) {
      sb.append("\n");
      sb.append(headers
          .stream()
          .map(h -> h.getName() + ":" + h.getValue())
          .sorted()
          .collect(Collectors.joining("\n")));
    }
    return sb.toString();
  }

  protected CloseableHttpResponse checkSuccess(@NonNull final CloseableHttpResponse response)
      throws Exception {
    LOG.log(DEBUG, "StringHttpClient.checkSuccess(CloseableHttpResponse)");

    final int code = response.getStatusLine().getStatusCode();
    if (code >= 200 && code <= 399) {
      LOG.log(DEBUG, "Response code " + code + " indicated success");
      return response;
    }

    LOG.log(ERROR, "Response code " + code + " did not indicate success");
    LOG.log(ERROR, EntityUtils.toString(response.getEntity()));
    throw new Exception("Response code " + code + " did not indicate success");
  }
}
