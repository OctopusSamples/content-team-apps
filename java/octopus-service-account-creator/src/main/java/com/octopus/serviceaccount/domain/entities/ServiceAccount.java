package com.octopus.serviceaccount.domain.entities;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
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
  private boolean isService;

  @Relationship("apiKey")
  private ApiKey apiKey;
}
