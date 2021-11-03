package com.octopus.githubactions.builders.dsl;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a step with name, id, uses, with, and env properties.
 */
@Builder
@Data
@RegisterForReflection
public class UsesWith implements Step {

  private String name;
  private String id;
  private String uses;
  private String ifProperty;
  private Map<String, String> with;
  private Map<String, String> env;
}
