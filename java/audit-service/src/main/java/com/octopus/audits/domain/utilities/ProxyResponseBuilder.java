package com.octopus.audits.domain.utilities;

import com.octopus.audits.application.lambda.ProxyResponse;
import lombok.NonNull;
import org.apache.commons.text.StringEscapeUtils;

/**
 * Utility class for building common responses.
 */
public class ProxyResponseBuilder {

  /**
   * Build an error object including the exception name. https://jsonapi.org/format/#error-objects
   *
   * @param ex The exception
   * @return The ProxyResponse representing the error.
   */
  public static ProxyResponse buildError(@NonNull final Exception ex, @NonNull final String origin) {
    return new ProxyResponse(
        "500", "{\"errors\": [{\"code\": \"" + ex.getClass().getCanonicalName() + "\"}]}", origin);
  }

  /**
   * Build an error object including the exception name and the body of the request that was sent.
   * https://jsonapi.org/format/#error-objects
   *
   * @param ex          The exception
   * @param requestBody The request body
   * @return The ProxyResponse representing the error.
   */
  public static ProxyResponse buildError(@NonNull final Exception ex, @NonNull final String requestBody, @NonNull final String origin) {
    return new ProxyResponse(
        "500",
        "{\"errors\": [{\"code\": \""
            + ex.getClass().getCanonicalName()
            + "\", \"meta\": {\"requestBody\": \""
            + StringEscapeUtils.escapeJson(requestBody)
            + "\"}}]}",
        origin);
  }

  /**
   * Build a error object for a 404 not found error. https://jsonapi.org/format/#error-objects
   *
   * @return The ProxyResponse representing the error.
   */
  public static ProxyResponse buildNotFound(@NonNull final String origin) {
    return new ProxyResponse("404", "{\"errors\": [{\"title\": \"Resource not found\"}]}", origin);
  }

  /**
   * Build an error object including the exception name. https://jsonapi.org/format/#error-objects
   *
   * @param ex The exception
   * @return The ProxyResponse representing the error.
   */
  public static ProxyResponse buildBadRequest(@NonNull final Exception ex, @NonNull final String origin) {
    return new ProxyResponse(
        "400", "{\"errors\": [{\"code\": \"" + ex.getClass().getCanonicalName() + "\"}]}", origin);
  }

  /**
   * Build an error object including the exception name. https://jsonapi.org/format/#error-objects
   *
   * @param ex The exception
   * @return The ProxyResponse representing the error.
   */
  public static ProxyResponse buildUnauthorizedRequest(@NonNull final Exception ex, @NonNull final String origin) {
    return new ProxyResponse("403", "{\"errors\": [{\"title\": \"Unauthorized\"}]}", origin);
  }
}
