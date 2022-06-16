
package com.octopus.products.application.lambda.impl;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.octopus.Constants;
import com.octopus.products.application.Paths;
import com.octopus.products.application.lambda.LambdaRequestHandler;
import com.octopus.products.domain.handlers.HealthHandler;
import com.octopus.lambda.ApiGatewayProxyResponseEventWithCors;
import com.octopus.lambda.ProxyResponseBuilder;
import com.octopus.lambda.RequestMatcher;
import io.vavr.control.Try;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.NonNull;

/**
 * Handle health check requests.
 */
@ApplicationScoped
public class LambdaRequestHandlerHealth implements LambdaRequestHandler {

  /**
   * A regular expression matching a health endpoint.
   */
  public static final Pattern HEALTH_RE =
      Pattern.compile(Paths.HEALTH_ENDPOINT + "/(GET|POST|[A-Za-z0-9]+/(GET|DELETE|PATCH))");

  @Inject
  RequestMatcher requestMatcher;

  @Inject
  ProxyResponseBuilder proxyResponseBuilder;

  @Inject
  HealthHandler healthHandler;

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
  @Override
  public Optional<APIGatewayProxyResponseEvent> handleRequest(@NonNull final APIGatewayProxyRequestEvent input) {
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
        .recover(e -> Optional.of(proxyResponseBuilder.buildError(e)))
        .get();
  }
}
