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
 * <p>
 * This entity provides a globally unique instance (an Octopus space in this case) for what are
 * otherwise only locally unique resources. That is to say, from the point of view of any individual
 * Octopus instance, a space with an ID of Spaces-1 is a unique resources. However, from the point of
 * view of this service, there are any number of spaces with an ID of Spaces-1, because this service
 * must consider all hosted instances.
 * <p>
 * We considered a number of options for globally unique references.
 * <p>
 * URNs initially looked like a good option, but they removed support for experimental namespaces,
 * which means technically there is no way to use a URN without a registered namespace.
 * <p>
 * Tag URIs (https://en.wikipedia.org/wiki/Tag_URI) could have been used, but this would have required
 * translating the Octopus domains into the tag URI authority.
 * <p>
 * At the end of the day, most resources that proxy services like this reference have their own
 * unique REST API endpoint, so it made sense to use that as the globally unique ID.
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
