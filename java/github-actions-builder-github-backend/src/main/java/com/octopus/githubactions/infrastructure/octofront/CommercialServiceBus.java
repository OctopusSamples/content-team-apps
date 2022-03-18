package com.octopus.githubactions.infrastructure.octofront;

import lombok.NonNull;

/**
 * Defines a service for sending user details to OctoFront.
 */
public interface CommercialServiceBus {

  /**
   * Send the details of a user that just logged in to the service bus.
   *
   * @param traceId A trace ID. Can be left blank.
   * @param body    The message body.
   */
  void sendUserDetails(String traceId, @NonNull String body);
}
