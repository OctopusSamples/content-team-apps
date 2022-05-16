package com.octopus.products.domain.entities;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a health check response.
 */
@Data
@Builder
@Type("healths")
public class Health {
  @Id
  public String endpoint;

  public String path;

  public String method;

  public String status;
}
