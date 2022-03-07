package com.octopus.githubrepo.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Represents an JSONAPI resource and database entity.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@Type("serviceaccount")
public class ServiceAccount {

  /**
   * The service account id
   */
  @Id
  private String id;

  /**
   * The service account name
   */
  @NotBlank
  private String username;

  /**
   * The service account description
   */
  @NotBlank
  private String displayName;

  /**
   * The service account description
   */
  @JsonProperty("isService")
  private boolean isService;

  @Relationship("apiKey")
  private ApiKey apiKey;
}
