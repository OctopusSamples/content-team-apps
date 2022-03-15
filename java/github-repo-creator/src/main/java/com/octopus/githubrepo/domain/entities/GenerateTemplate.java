package com.octopus.githubrepo.domain.entities;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents a request to generate a GitHub repo.
 */
@Type("generatetemplate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
public class GenerateTemplate {
  @Id
  private String id;

  @NotBlank
  private String generator;

  private Map<String, String> options;
}
