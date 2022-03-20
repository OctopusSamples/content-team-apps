package com.octopus.githubrepo.application.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.common.net.HttpHeaders;
import com.octopus.Constants;
import com.octopus.exceptions.InvalidInputException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.lambda.ApiGatewayProxyResponseEventWithCors;
import com.octopus.lambda.LambdaHttpCookieExtractor;
import com.octopus.lambda.LambdaHttpHeaderExtractor;
import com.octopus.lambda.ProxyResponseBuilder;
import com.octopus.githubrepo.domain.ServiceConstants;
import com.octopus.githubrepo.domain.handlers.HealthHandler;
import com.octopus.githubrepo.domain.handlers.ServiceAccountHandler;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;

/**
 * The Lambda entry point used to return audit resources.
 */
@Named("ServiceAccounts")
@ApplicationScoped
public class ServiceAccountApi implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private static final String API_PATH = "/api/serviceaccounts";
  private static final String HEALTH_PATH = "/health/serviceaccounts";

  /**
   * A regular expression matching the collection of entities.
   */
  public static final Pattern ROOT_RE = Pattern.compile(API_PATH + "/?");
  /**
   * A regular expression matching a health endpoint.
   */
  public static final Pattern HEALTH_RE =
      Pattern.compile(HEALTH_PATH + "/(GET|POST|[A-Za-z0-9]+/(GET|DELETE|PATCH))");

  @Inject
  ServiceAccountHandler serviceAccountHandler;

  @Inject
  HealthHandler healthHandler;

  @Inject
  LambdaHttpHeaderExtractor lambdaHttpHeaderExtractor;

  @Inject
  LambdaHttpCookieExtractor lambdaHttpCookieExtractor;

  @Inject
  ProxyResponseBuilder proxyResponseBuilder;


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
        .orElse(proxyResponseBuilder.buildNotFound());
  }

  /**
   * Health checks sit parallel to the /api endpoint under /health. The health endpoints mirror the
   * API, but with an additional path that indicates the http method. So, for example, a GET request
   * to /health/audits/GET will return 200 OK if the service responding to /api/audits is able to
   * service a GET request, and a GET request to /health/audits/1/DELETE will return 200 OK if the
   * service responding to /api/audits/1 is available to service a DELETE request.
   *
   * <p>This approach was taken to support the fact that Lambdas may well have unique services
   * responding to each individual endpoint. For example, you may have a dedicated lambda fetching
   * resource collections (i.e. /api/audits), and a dedicated lambda fetching individual resources
   * (i.e. /api/audits/1). The health of these lambdas may be independent of one another.
   *
   * <p>This is unlike a traditional web service, where it is usually taken for granted that a
   * single application responds to all these requests, and therefore a single health endpoint can
   * represent the status of all endpoints.
   *
   * <p>By ensuring every path has a matching health endpoint, we allow clients to verify the
   * status of the service without having to know which lambdas respond to which requests. This does
   * mean that a client may need to verify the health of half a dozen endpoints to fully determine
   * the state of the client's dependencies, but this is a more accurate representation of the
   * health of the system.
   *
   * <p>This particular service will typically be deployed with one lambda responding to many
   * endpoints, but clients can not assume this is always the case, and must check the health of
   * each endpoint to accurately evaluate the health of the service.
   *
   * @param input The request details
   * @return The optional proxy response
   */
  private Optional<APIGatewayProxyResponseEvent> checkHealth(
      final APIGatewayProxyRequestEvent input) {

    if (requestIsMatch(input, HEALTH_RE, Constants.Http.GET_METHOD)) {
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

    return Optional.empty();
  }

  /**
   * Create a audit.
   *
   * @param input The Lambda request.
   * @return The Lambda response.
   */
  private Optional<APIGatewayProxyResponseEvent> createOne(
      final APIGatewayProxyRequestEvent input) {
    try {
      if (requestIsMatch(input, ROOT_RE, Constants.Http.POST_METHOD)) {
        return Optional.of(
            new ApiGatewayProxyResponseEventWithCors()
                .withStatusCode(200)
                .withBody(
                    serviceAccountHandler.create(
                        getBody(input),
                        lambdaHttpHeaderExtractor.getFirstHeader(input, HttpHeaders.AUTHORIZATION)
                            .orElse(null),
                        lambdaHttpHeaderExtractor.getFirstHeader(input,
                            Constants.SERVICE_AUTHORIZATION_HEADER).orElse(null),
                        lambdaHttpCookieExtractor.getCookieValue(input,
                            ServiceConstants.OCTOPUS_SESSION_COOKIE).orElse(""))));
      }
    } catch (final UnauthorizedException e) {
      return Optional.of(proxyResponseBuilder.buildUnauthorizedRequest(e));
    } catch (final InvalidInputException e) {
      return Optional.of(proxyResponseBuilder.buildBadRequest(e));
    } catch (final Exception e) {
      e.printStackTrace();
      return Optional.of(proxyResponseBuilder.buildError(e, getBody(input)));
    }

    return Optional.empty();
  }

  /**
   * Determine if the Lambda request matches path and method.
   *
   * @param input  The Lambda request.
   * @param regex  The path regex.
   * @param method The HTTP method.
   * @return true if this request matches the supplied values, and false otherwise.
   */
  public boolean requestIsMatch(
      @NonNull final APIGatewayProxyRequestEvent input,
      @NonNull final Pattern regex,
      @NonNull final String method) {
    final String path = ObjectUtils.defaultIfNull(input.getPath(), "");
    final String requestMethod = ObjectUtils.defaultIfNull(input.getHttpMethod(), "").toLowerCase();
    return regex.matcher(path).matches() && method.toLowerCase().equals(requestMethod);
  }

  /**
   * Get the request body, and deal with the fact that it may be base64 encoded.
   *
   * @param input The Lambda request
   * @return The decoded request body
   */
  private String getBody(final APIGatewayProxyRequestEvent input) {
    final String body = ObjectUtils.defaultIfNull(input.getBody(), "");
    final String isBase64Encoded =
        ObjectUtils.defaultIfNull(input.getIsBase64Encoded(), "").toString().toLowerCase();

    if ("true".equals(isBase64Encoded)) {
      return new String(Base64.getDecoder().decode(body));
    }

    return body;
  }
}
