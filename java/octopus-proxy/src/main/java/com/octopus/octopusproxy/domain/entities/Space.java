package com.octopus.octopusproxy.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents an JSONAPI resource.
 */
@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@Type("spaces")
public class Space {

  /**
   * The ID of an entity from the point of view of this proxy is the URL to the space resource e.g.
   * https://mattc.octopus.app/spaces/Space-1. This is because this service is a proxy over
   * all the Octopus spaces everywhere, and must distinguish between different Octopus instances.
   */
  @Id
  private String id;

  /**
   * This is the local space ID e.g. Space-1
   */
  @JsonProperty("Id")
  private String entityId;

  /**
   * This is the space name.
   */
  @JsonProperty("Name")
  private String name;
}
