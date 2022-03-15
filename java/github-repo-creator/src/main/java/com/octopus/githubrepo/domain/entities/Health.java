package com.octopus.githubrepo.domain.entities;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents a health check response.
 */
@Data
@Builder
@Jacksonized
@Type("healths")
public class Health {
  @Id
  public String endpoint;

  public String path;
  public String method;
  public String status;
}
