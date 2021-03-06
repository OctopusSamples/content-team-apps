package com.octopus.githubproxy.application.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.common.net.HttpHeaders;
import com.octopus.Constants;
import com.octopus.exceptions.EntityNotFoundException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.githubproxy.ServiceConstants;
import com.octopus.githubproxy.application.Paths;
import com.octopus.githubproxy.domain.handlers.HealthHandler;
import com.octopus.githubproxy.domain.handlers.ResourceHandler;
import com.octopus.lambda.ApiGatewayProxyResponseEventWithCors;
import com.octopus.lambda.LambdaHttpCookieExtractor;
import com.octopus.lambda.LambdaHttpHeaderExtractor;
import com.octopus.lambda.ProxyResponseBuilder;
import com.octopus.lambda.RequestMatcher;
import com.octopus.utilties.RegExUtils;
import io.vavr.control.Try;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import lombok.NonNull;

/**
 * The Lambda entry point used to return resources.
 */
@Named("ResourceHandler")
@ApplicationScoped
public class LambdaRequestHanlder implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  /**
   * A regular expression matching a single entity.
   */
  public static final Pattern INDIVIDUAL_RE = Pattern.compile(Paths.API_ENDPOINT + "/(?<id>.+)");
  public static final Pattern WORKFLOW_RUNS_INDIVIDUAL_RE = Pattern.compile(
      Paths.WORKFLOW_RUNS_API_ENDPOINT + "/(?<id>.+)");
  /**
   * A regular expression matching a health endpoint.
   */
  public static final Pattern HEALTH_RE =
      Pattern.compile(Paths.HEALTH_ENDPOINT + "/(GET|POST|[A-Za-z0-9]+/(GET|DELETE|PATCH))");

  @Inject
  ResourceHandler resourceHandler;

  @Inject
  HealthHandler healthHandler;

  @Inject
  LambdaHttpHeaderExtractor lambdaHttpHeaderExtractor;

  @Inject
  LambdaHttpCookieExtractor lambdaHttpCookieExtractor;

  @Inject
  ProxyResponseBuilder proxyResponseBuilder;

  @Inject
  RegExUtils regExUtils;

  @Inject
  RequestMatcher requestMatcher;


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
    return getOne(input)
        .or(() -> checkHealth(input))
        .orElseGet(() -> proxyResponseBuilder.buildPathNotFound());
  }

  /**
   * Health checks sit parallel to the /api endpoint under /health. The health endpoints mirror the API, but with an additional path that indicates the http
   * method. So, for example, a GET request to /health/resources/GET will return 200 OK if the service responding to /api/resources is able to service a GET
   * request, and a GET request to /health/resources/1/DELETE will return 200 OK if the service responding to /api/resources/1 is available to service a DELETE
   * request.
   *
   * <p>This approach was taken to support the fact that Lambdas may well have unique services
   * responding to each individual endpoint. For example, you may have a dedicated lambda fetching resource collections (i.e. /api/resources), and a dedicated
   * lambda fetching individual resources (i.e. /api/resources/1). The health of these lambdas may be independent of one another.
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

    try {
      return Optional.of(
          new ApiGatewayProxyResponseEventWithCors()
              .withStatusCode(200)
              .withBody(healthHandler.getHealth(
                  input.getPath().substring(0, input.getPath().lastIndexOf("/")),
                  input.getPath().substring(input.getPath().lastIndexOf("/")))));
    } catch (final Exception e) {
      e.printStackTrace();
      return Optional.of(proxyResponseBuilder.buildError(e));
    }

  }

  /**
   * Return a resources.
   *
   * @param input The Lambda request.
   * @return The Lambda response.
   */
  private Optional<APIGatewayProxyResponseEvent> getOne(final APIGatewayProxyRequestEvent input) {

    if (!requestMatcher.requestIsMatch(input, INDIVIDUAL_RE, Constants.Http.GET_METHOD)) {
      return Optional.empty();
    }

    final Optional<String> id = regExUtils.getGroup(INDIVIDUAL_RE, input.getPath(), "id");

    if (id.isEmpty()) {
      return Optional.of(proxyResponseBuilder.buildNotFound());
    }

    return Try.of(() -> resourceHandler.getOne(
            id.get(),
            lambdaHttpHeaderExtractor.getAllHeaders(input, Constants.DATA_PARTITION_HEADER),
            lambdaHttpHeaderExtractor.getFirstHeader(input, HttpHeaders.AUTHORIZATION).orElse(null),
            lambdaHttpHeaderExtractor.getFirstHeader(input, Constants.SERVICE_AUTHORIZATION_HEADER).orElse(null),
            lambdaHttpCookieExtractor.getCookieValue(input, ServiceConstants.GITHUB_SESSION_COOKIE).orElse(""))
            .await().indefinitely())
        .map(entity -> Optional.of(
            new ApiGatewayProxyResponseEventWithCors()
                .withStatusCode(200)
                .withBody(entity)))
        .recover(UnauthorizedException.class, e -> Optional.of(proxyResponseBuilder.buildUnauthorizedRequest(e)))
        .recover(EntityNotFoundException.class, e -> Optional.of(proxyResponseBuilder.buildNotFound()))
        .recover(Exception.class, e -> Optional.of(proxyResponseBuilder.buildError(e)))
        .get();
  }
}
