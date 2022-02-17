package com.octopus.audits.domain.jsonapi;

import java.util.List;

/**
 * Represents a service used to verify the JSONAPI accept headers.
 */
public interface AcceptHeaderVerifier {

  /**
   * Clients that include the JSON:API media type in their Accept header MUST specify the media
   * type there at least once without any media type parameters.
   *
   * @param acceptHeader The list of accept headers.
   */
  void checkAcceptHeader(final List<String> acceptHeader);
}
