package com.octopus.githubactions.builders.dsl;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UsesWith implements Step {
  private String name;
  private String id;
  private String uses;
  private Map<String, String> with;
  private Map<String, String> env;
}
