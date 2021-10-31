package com.octopus.githubactions.builders;

import com.octopus.githubactions.builders.dsl.UsesStep;
import com.octopus.githubactions.builders.dsl.Workflow;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Builds preconfigured instances of SnakeYAML.
 */
public final class SnakeYamlFactory {

  /**
   * Builds an instance of SnakeYAML configured to generate workflow DSLs.
   *
   * @return A configured instance of SnakeYAML.
   */
  public static Yaml getConfiguredYaml() {
    final Representer representer = new Representer();
    representer.addClassTag(Workflow.class, Tag.MAP);
    representer.addClassTag(UsesStep.class, Tag.MAP);

    final DumperOptions options = new DumperOptions();
    options.setDefaultFlowStyle(FlowStyle.BLOCK);
    return new Yaml(representer, options);
  }
}
