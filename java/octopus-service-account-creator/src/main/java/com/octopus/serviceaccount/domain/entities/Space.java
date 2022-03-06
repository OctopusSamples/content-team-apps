package com.octopus.serviceaccount.domain.entities;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import java.util.Collection;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@Type("space")
public class Space {
  @Id
  private String id;

  @NotBlank
  private String name;

  @Relationship("spaceManagersTeamMembers")
  private Collection<ServiceAccount> spaceManagersTeamMembers;
}
