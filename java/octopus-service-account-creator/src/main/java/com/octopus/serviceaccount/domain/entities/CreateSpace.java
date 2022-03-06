package com.octopus.serviceaccount.domain.entities;

import com.github.jasminb.jsonapi.annotations.Type;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@Type("createspace")
public class CreateSpace extends Space {
  /**
   * The octopus server to create the account against
   */
  @NotBlank
  private String octopusServer;
}
