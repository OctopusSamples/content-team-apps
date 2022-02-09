package com.octopus.octopub.domain;

/**
 * Constants used by the service.
 */
public final class Constants {
  public static final String CREATED_ACTION = "CREATED";
  public static final String DELETED_ACTION = "DELETED";
  public static final String UPDATED_ACTION = "UPDATED";
  public static final String UPDATED_FAILED_PARTITION_MISMATCH_ACTION =
      "UPDATED_FAILED_PARTITION_MISMATCH";
  public static final String DEFAULT_PARTITION = "main";
  public static final String ACCEPT_PARTITION_INFO = "dataPartition";
  public static final String JSONAPI_CONTENT_TYPE = "application/vnd.api+json";
}
