package com.octopus.audits.application.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.octopus.audits.application.Constants;
import com.octopus.audits.domain.exceptions.EntityNotFound;
import com.octopus.audits.domain.exceptions.InvalidInput;
import com.octopus.audits.domain.exceptions.Unauthorized;
import com.octopus.audits.domain.handlers.AuditsHandler;
import com.octopus.audits.domain.handlers.HealthHandler;
import com.octopus.lambda.LambdaHttpHeaderExtractor;
import com.octopus.lambda.LambdaHttpValueExtractor;
import cz.jirutka.rsql.parser.RSQLParserException;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.text.StringEscapeUtils;

/** The Lambda entry point used to return audit resources. */
@Named("Audits")
public class AuditApi implements RequestHandler<APIGatewayProxyRequestEvent, ProxyResponse> {

  /** A regular expression matching the collection of entities. */
  private static final Pattern ROOT_RE = Pattern.compile("/api/audits/?");
  /** A regular expression matching a single entity. */
  private static final Pattern INDIVIDUAL_RE = Pattern.compile("/api/audits/(?<id>\\d+)");
  /** A regular expression matching a health endpoint. */
  private static final Pattern HEALTH_RE =
      Pattern.compile("/health/audits/(GET|POST|[A-Za-z0-9]+/(GET|DELETE|PATCH))");

  @Inject
  AuditsHandler auditsHandler;

  @Inject
  HealthHandler healthHandler;

  @Inject
  LambdaHttpValueExtractor lambdaHttpValueExtractor;

  @Inject
  LambdaHttpHeaderExtractor lambdaHttpHeaderExtractor;

  /**
   * See https://github.com/quarkusio/quarkus/issues/5811 for why we need @Transactional.
   *
   * @param input The request details
   * @param context The request context
   * @return The proxy response
   */
  @Override
  @Transactional
  public ProxyResponse handleRequest(
      @NonNull final APIGatewayProxyRequestEvent input, @NonNull final Context context) {

    /*
     Lambdas don't enjoy the same middleware and framework support as web servers, so we are
     on our own with functionality such as routing requests to handlers. This code simply calls
     each handler to find the first one that responds to the request.
    */
    return getAll(input)
        .or(() -> getOne(input))
        .or(() -> createOne(input))
        .or(() -> checkHealth(input))
        .orElse(buildNotFound());
  }

  /**
   * Health checks sit parallel to the /api endpoint under /health. The health endpoints mirror the
   * API, but with an additional path that indicates the http method. So, for example, a GET request
   * to /health/audits/GET will return 200 OK if the service responding to /api/audits is able
   * to service a GET request, and a GET request to /health/audits/1/DELETE will return 200 OK if
   * the service responding to /api/audits/1 is available to service a DELETE request.
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
   * <p>By ensuring every path has a matching health endpoint, we allow clients to verify the status
   * of the service without having to know which lambdas respond to which requests. This does mean
   * that a client may need to verify the health of half a dozen endpoints to fully determine the
   * state of the client's dependencies, but this is a more accurate representation of the health of
   * the system.
   *
   * <p>This particular service will typically be deployed with one lambda responding to many
   * endpoints, but clients can not assume this is always the case, and must check the health of
   * each endpoint to accurately evaluate the health of the service.
   *
   * @param input The request details
   * @return The optional proxy response
   */
  private Optional<ProxyResponse> checkHealth(@NonNull final APIGatewayProxyRequestEvent input) {

    if (requestIsMatch(input, HEALTH_RE, Constants.GET_METHOD)) {
      try {
        return Optional.of(
            new ProxyResponse(
                "200",
                healthHandler.getHealth(
                    input.getPath().substring(0, input.getPath().lastIndexOf("/")),
                    input.getPath().substring(input.getPath().lastIndexOf("/")))));
      } catch (final Exception e) {
        e.printStackTrace();
        return Optional.of(buildError(e));
      }
    }

    return Optional.empty();
  }

  /**
   * Get a collection of audits.
   *
   * @param input The Lambda request.
   * @return The Lambda response.
   */
  private Optional<ProxyResponse> getAll(@NonNull final APIGatewayProxyRequestEvent input) {
    try {
      if (requestIsMatch(input, ROOT_RE, Constants.GET_METHOD)) {
        return Optional.of(
            new ProxyResponse(
                "200",
                auditsHandler.getAll(
                    lambdaHttpHeaderExtractor.getAllHeaders(input, Constants.DATA_PARTITION_HEADER),
                    lambdaHttpValueExtractor.getQueryParam(input, Constants.FILTER_QUERY_PARAM).orElse(null),
                    lambdaHttpValueExtractor.getQueryParam(input, Constants.PAGE_OFFSET_QUERY_PARAM).orElse(null),
                    lambdaHttpValueExtractor.getQueryParam(input, Constants.PAGE_LIMIT_QUERY_PARAM).orElse(null),
                    lambdaHttpHeaderExtractor.getFirstHeader(input, Constants.AUTHORIZATION_HEADER).orElse(null),
                    lambdaHttpHeaderExtractor.getFirstHeader(input, Constants.SERVICE_AUTHORIZATION_HEADER).orElse(null))));
      }
    } catch (final Unauthorized e) {
      return Optional.of(buildUnauthorizedRequest(e));
    } catch (final RSQLParserException e) {
      return Optional.of(buildBadRequest(e));
    } catch (final Exception e) {
      e.printStackTrace();
      return Optional.of(buildError(e));
    }

    return Optional.empty();
  }

  /**
   * Return a audit.
   *
   * @param input The Lambda request.
   * @return The Lambda response.
   */
  private Optional<ProxyResponse> getOne(@NonNull final APIGatewayProxyRequestEvent input) {
    try {

      if (requestIsMatch(input, INDIVIDUAL_RE, Constants.GET_METHOD)) {
        final Optional<String> id = getGroup(INDIVIDUAL_RE, input.getPath(), "id");

        if (id.isPresent()) {
          final String entity =
              auditsHandler.getOne(
                  id.get(),
                  lambdaHttpHeaderExtractor.getAllHeaders(input, Constants.DATA_PARTITION_HEADER),
                  lambdaHttpHeaderExtractor.getFirstHeader(input, Constants.AUTHORIZATION_HEADER).orElse(null),
                  lambdaHttpHeaderExtractor.getFirstHeader(input, Constants.SERVICE_AUTHORIZATION_HEADER).orElse(null));

          return Optional.of(new ProxyResponse("200", entity));
        }
        return Optional.of(buildNotFound());
      }
    } catch (final Unauthorized e) {
      return Optional.of(buildUnauthorizedRequest(e));
    } catch (final EntityNotFound ex) {
      return Optional.of(buildNotFound());
    } catch (final Exception e) {
      e.printStackTrace();
      return Optional.of(buildError(e));
    }

    return Optional.empty();
  }

  /**
   * Create a audit.
   *
   * @param input The Lambda request.
   * @return The Lambda response.
   */
  private Optional<ProxyResponse> createOne(@NonNull final APIGatewayProxyRequestEvent input) {
    try {
      if (requestIsMatch(input, ROOT_RE, Constants.POST_METHOD)) {
        return Optional.of(
            new ProxyResponse(
                "200",
                auditsHandler.create(
                    getBody(input),
                    lambdaHttpHeaderExtractor.getAllHeaders(input, Constants.DATA_PARTITION_HEADER),
                    lambdaHttpHeaderExtractor.getFirstHeader(input, Constants.AUTHORIZATION_HEADER).orElse(null),
                    lambdaHttpHeaderExtractor.getFirstHeader(input, Constants.SERVICE_AUTHORIZATION_HEADER).orElse(null))));
      }
    } catch (final Unauthorized e) {
      return Optional.of(buildUnauthorizedRequest(e));
    } catch (final InvalidInput e) {
      return Optional.of(buildBadRequest(e));
    } catch (final Exception e) {
      e.printStackTrace();
      return Optional.of(buildError(e, getBody(input)));
    }

    return Optional.empty();
  }

  /**
   * Get the regex group from the pattern for the input.
   *
   * @param pattern The regex pattern.
   * @param input The input to apply the pattern to.
   * @param group The group name to return.
   * @return The regex group value.
   */
  private Optional<String> getGroup(
      @NonNull final Pattern pattern, final Object input, @NonNull final String group) {
    if (input == null) {
      return Optional.empty();
    }

    final Matcher matcher = pattern.matcher(input.toString());

    if (matcher.find()) {
      return Optional.of(matcher.group(group));
    }

    return Optional.empty();
  }

  /**
   * Determine if the Lambda request matches path and method.
   *
   * @param input The Lmabda request.
   * @param regex The path regex.
   * @param method The HTTP method.
   * @return true if this request matches the supplied values, and false otherwise.
   */
  private boolean requestIsMatch(
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
   * @return The unencoded request body
   */
  private String getBody(@NonNull final APIGatewayProxyRequestEvent input) {
    final String body = ObjectUtils.defaultIfNull(input.getBody(), "");
    final String isBase64Encoded =
        ObjectUtils.defaultIfNull(input.getIsBase64Encoded(), "").toString().toLowerCase();

    if ("true".equals(isBase64Encoded)) {
      return new String(Base64.getDecoder().decode(body));
    }

    return body;
  }

  /**
   * Build an error object including the exception name and the body of the request that was sent.
   * https://jsonapi.org/format/#error-objects
   *
   * @param ex The exception
   * @param requestBody The request body
   * @return The ProxyResponse representing the error.
   */
  private ProxyResponse buildError(@NonNull final Exception ex, final String requestBody) {
    return new ProxyResponse(
        "500",
        "{\"errors\": [{\"code\": \""
            + ex.getClass().getCanonicalName()
            + "\", \"meta\": {\"requestBody\": \""
            + StringEscapeUtils.escapeJson(requestBody)
            + "\"}}]}");
  }

  /**
   * Build an error object including the exception name. https://jsonapi.org/format/#error-objects
   *
   * @param ex The exception
   * @return The ProxyResponse representing the error.
   */
  private ProxyResponse buildError(@NonNull final Exception ex) {
    return new ProxyResponse(
        "500", "{\"errors\": [{\"code\": \"" + ex.getClass().getCanonicalName() + "\"}]}");
  }

  /**
   * Build a error object for a 404 not found error. https://jsonapi.org/format/#error-objects
   *
   * @return The ProxyResponse representing the error.
   */
  private ProxyResponse buildNotFound() {
    return new ProxyResponse("404", "{\"errors\": [{\"title\": \"Resource not found\"}]}");
  }

  /**
   * Build an error object including the exception name. https://jsonapi.org/format/#error-objects
   *
   * @param ex The exception
   * @return The ProxyResponse representing the error.
   */
  private ProxyResponse buildBadRequest(@NonNull final Exception ex) {
    return new ProxyResponse(
        "400", "{\"errors\": [{\"code\": \"" + ex.getClass().getCanonicalName() + "\"}]}");
  }

  /**
   * Build an error object including the exception name. https://jsonapi.org/format/#error-objects
   *
   * @param ex The exception
   * @return The ProxyResponse representing the error.
   */
  private ProxyResponse buildUnauthorizedRequest(@NonNull final Exception ex) {
    return new ProxyResponse("403", "{\"errors\": [{\"title\": \"Unauthorized\"}]}");
  }
}
