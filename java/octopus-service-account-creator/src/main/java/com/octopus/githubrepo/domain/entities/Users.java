package com.octopus.githubrepo.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a collection og users returned from the Octopus API.
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Users {

  @JsonProperty("ItemType")
  private String itemType;
  @JsonProperty("Items")
  private Collection<User> items;

}
