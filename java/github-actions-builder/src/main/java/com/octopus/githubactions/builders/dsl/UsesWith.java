package com.octopus.githubactions.builders.dsl;

import java.util.Map;
import lombok.Builder;

/**
 * Represents a step with name, id, uses, with, and env properties.
 */
@Builder
@lombok.Data
public class UsesWith implements Step {

  private String name;
  private String id;
  private String uses;
  private String ifProperty;
  private Map<String, String> with;
  private Map<String, String> env;
}
