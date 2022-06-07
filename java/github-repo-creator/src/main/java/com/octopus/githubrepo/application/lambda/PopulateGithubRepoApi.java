package com.octopus.githubrepo.application.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.common.net.HttpHeaders;
import com.octopus.Constants;
import com.octopus.exceptions.InvalidInputException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.githubrepo.domain.ServiceConstants;
import com.octopus.githubrepo.domain.handlers.GitHubRepoHandler;
import com.octopus.githubrepo.domain.handlers.HealthHandler;
import com.octopus.lambda.ApiGatewayProxyResponseEventWithCors;
import com.octopus.lambda.LambdaHttpCookieExtractor;
import com.octopus.lambda.LambdaHttpHeaderExtractor;
import com.octopus.lambda.ProxyResponseBuilder;
import com.octopus.lambda.RequestBodyExtractor;
import com.octopus.lambda.RequestMatcher;
import io.quarkus.logging.Log;
import io.vavr.control.Try;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import lombok.NonNull;

/**
 * The Lambda entry point used to populate a GitHub repo.
 *
 * <p>Note that Quarkus (at least at 2.7) had issues running both a web server and the mock Lambda
 * server at the same time. This is solved by not compiling the io.quarkus:quarkus-amazon-lambda dependency into builds that expose a web server.
 *
 * <p>To include the io.quarkus:quarkus-amazon-lambda dependency, enable the "lambda" Maven profile
 * by running:
 *
 * <p>mvn -Pnative -Plambda package
 */
@Named("PopulateGithubRepo")
@ApplicationScoped
public class PopulateGithubRepoApi implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private static final String API_PATH = "/api/populategithubrepo";
  private static final String HEALTH_PATH = "/health/populategithubrepo";

  /**
   * A regular expression matching the collection of entities.
   */
  public static final Pattern ROOT_RE = Pattern.compile(API_PATH + "/?");
  /**
   * A regular expression matching a health endpoint.
   */
  public static final Pattern HEALTH_RE = Pattern.compile(HEALTH_PATH + "/POST");

  @Inject
  GitHubRepoHandler gitHubRepoHandler;

  @Inject
  HealthHandler healthHandler;

  @Inject
  LambdaHttpHeaderExtractor lambdaHttpHeaderExtractor;

  @Inject
  LambdaHttpCookieExtractor lambdaHttpCookieExtractor;

  @Inject
  ProxyResponseBuilder proxyResponseBuilder;

  @Inject
  RequestMatcher requestMatcher;

  @Inject
  RequestBodyExtractor requestBodyExtractor;


  /**
   * See https://github.com/quarkusio/quarkus/issues/5811 for why we need @Transactional.
   *
   * @param input   The request details
   * @param context The request context
   * @return The proxy response
   */
  @Override
  @Transactional
  public APIGatewayProxyResponseEvent handleRequest(
      @NonNull final APIGatewayProxyRequestEvent input, @NonNull final Context context) {

    /*
     Lambdas don't enjoy the same middleware and framework support as web servers (although
     https://quarkus.io/guides/amazon-lambda-http is looking like a good option when it
     comes out of preview), so we are on our own with functionality such as routing requests to
     handlers. This code simply calls each handler to find the first one that responds to the request.
    */
    return createOne(input)
        .or(() -> checkHealth(input))
        .orElseGet(() -> notFound(input));
  }

  /**
   * Health checks sit parallel to the /api endpoint under /health. The health endpoints mirror the API, but with an additional path that indicates the http
   * method. So, for example, a GET request to /health/audits/GET will return 200 OK if the service responding to /api/audits is able to service a GET request,
   * and a GET request to /health/audits/1/DELETE will return 200 OK if the service responding to /api/audits/1 is available to service a DELETE request.
   *
   * <p>This approach was taken to support the fact that Lambdas may well have unique services
   * responding to each individual endpoint. For example, you may have a dedicated lambda fetching resource collections (i.e. /api/audits), and a dedicated
   * lambda fetching individual resources (i.e. /api/audits/1). The health of these lambdas may be independent of one another.
   *
   * <p>This is unlike a traditional web service, where it is usually taken for granted that a
   * single application responds to all these requests, and therefore a single health endpoint can represent the status of all endpoints.
   *
   * <p>By ensuring every path has a matching health endpoint, we allow clients to verify the
   * status of the service without having to know which lambdas respond to which requests. This does mean that a client may need to verify the health of half a
   * dozen endpoints to fully determine the state of the client's dependencies, but this is a more accurate representation of the health of the system.
   *
   * <p>This particular service will typically be deployed with one lambda responding to many
   * endpoints, but clients can not assume this is always the case, and must check the health of each endpoint to accurately evaluate the health of the
   * service.
   *
   * @param input The request details
   * @return The optional proxy response
   */
  private Optional<APIGatewayProxyResponseEvent> checkHealth(
      final APIGatewayProxyRequestEvent input) {

    if (!requestMatcher.requestIsMatch(input, HEALTH_RE, Constants.Http.GET_METHOD)) {
      return Optional.empty();
    }

    return Try.of(() -> Optional.of(
            new ApiGatewayProxyResponseEventWithCors()
                .withStatusCode(200)
                .withBody(healthHandler.getHealth(
                    input.getPath().substring(0, input.getPath().lastIndexOf("/")),
                    input.getPath().substring(input.getPath().lastIndexOf("/"))))))
        .onFailure(Throwable::printStackTrace)
        .recover(e -> Optional.of(proxyResponseBuilder.buildError(e, requestBodyExtractor.getBody(input))))
        .get();
  }

  /**
   * Create a github commit. Note this endpoint returns a 202, as the actual commit is created in an async operation after this request has returned. The
   * returned entity contains the details of the repo that the commit will be placed into.
   *
   * @param input The Lambda request.
   * @return The Lambda response.
   */
  private Optional<APIGatewayProxyResponseEvent> createOne(final APIGatewayProxyRequestEvent input) {

    if (!requestMatcher.requestIsMatch(input, ROOT_RE, Constants.Http.POST_METHOD)) {
      return Optional.empty();
    }

    return Try.of(() -> Optional.of(
            new ApiGatewayProxyResponseEventWithCors()
                .withStatusCode(201)
                .withBody(
                    gitHubRepoHandler.create(
                        requestBodyExtractor.getBody(input),
                        lambdaHttpHeaderExtractor.getFirstHeader(input, HttpHeaders.AUTHORIZATION).orElse(null),
                        lambdaHttpHeaderExtractor.getFirstHeader(input, Constants.SERVICE_AUTHORIZATION_HEADER).orElse(null),
                        lambdaHttpHeaderExtractor.getFirstHeader(input, Constants.ROUTING_HEADER).orElse(null),
                        lambdaHttpCookieExtractor.getCookieValue(input, ServiceConstants.GITHUB_SESSION_COOKIE).orElse("")))))
        .recover(UnauthorizedException.class, e -> Optional.of(proxyResponseBuilder.buildUnauthorizedRequest(e)))
        .recover(InvalidInputException.class, e -> Optional.of(proxyResponseBuilder.buildBadRequest(e)))
        .recover(IllegalArgumentException.class, e -> Optional.of(proxyResponseBuilder.buildBadRequest(e)))
        .onFailure(Throwable::printStackTrace)
        .recover(e -> Optional.of(proxyResponseBuilder.buildError(e, requestBodyExtractor.getBody(input))))
        .get();
  }

  private APIGatewayProxyResponseEvent notFound(@NonNull final APIGatewayProxyRequestEvent input) {
    Log.info("PopulateGithubRepoApi reported path to " + input.getPath() + " was not found");
    return proxyResponseBuilder.buildNotFound();
  }
}
