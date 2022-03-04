package com.octopus.serviceaccount.domain.entities;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Represents an JSONAPI resource and database entity.
 */
@Data
@Type("serviceaccount")
public class ServiceAccount {

  /**
   * The service account id
   */
  @Id
  public String id;

  /**
   * The service account name
   */
  @NotBlank
  public String username;

  /**
   * The service account description
   */
  @NotBlank
  public String displayName;

  /**
   * The service account description
   */
  public boolean isService;
}
