package com.octopus.githubactions.builders;

import com.octopus.githubactions.builders.dsl.Build;
import com.octopus.githubactions.builders.dsl.On;
import com.octopus.githubactions.builders.dsl.RunStep;
import com.octopus.githubactions.builders.dsl.UsesWith;
import com.octopus.githubactions.builders.dsl.Workflow;
import com.octopus.githubactions.builders.dsl.WorkflowDispatch;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/** Builds preconfigured instances of SnakeYAML. */
public final class SnakeYamlFactory {

  /**
   * Builds an instance of SnakeYAML configured to generate workflow DSLs.
   *
   * @return A configured instance of SnakeYAML.
   */
  public static Yaml getConfiguredYaml() {
    final Representer representer =
        new Representer() {
          @Override
          protected NodeTuple representJavaBeanProperty(
              final Object javaBean,
              final Property property,
              final Object propertyValue,
              final Tag customTag) {
            // if value of property is null, ignore it.
            if (propertyValue == null) {
              return null;
            } else {
              return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
            }
          }
        };

    representer.addClassTag(Workflow.class, Tag.MAP);
    representer.addClassTag(UsesWith.class, Tag.MAP);
    representer.addClassTag(RunStep.class, Tag.MAP);

    final TypeDescription onDesc = new TypeDescription(On.class);
    onDesc.substituteProperty(
        "workflow_dispatch", WorkflowDispatch.class, "getWorkflowDispatch", "setWorkflowDispatch");
    onDesc.setExcludes("workflowDispatch");
    representer.addTypeDescription(onDesc);

    final TypeDescription buildDesc = new TypeDescription(Build.class);
    buildDesc.substituteProperty("runs-on", String.class, "getRunsOn", "setRunsOn");
    buildDesc.setExcludes("runsOn");
    representer.addTypeDescription(buildDesc);

    final TypeDescription runDesc = new TypeDescription(RunStep.class);
    runDesc.substituteProperty("working-directory", String.class, "getWorkingDirectory", "setWorkingDirectory");
    runDesc.substituteProperty("if", String.class, "getIfProperty", "setIfProperty");
    runDesc.setExcludes("workingDirectory", "ifProperty");
    representer.addTypeDescription(runDesc);

    final TypeDescription usesWith = new TypeDescription(UsesWith.class);
    usesWith.substituteProperty("if", String.class, "getIfProperty", "setIfProperty");
    usesWith.setExcludes("ifProperty");
    representer.addTypeDescription(usesWith);

    final DumperOptions options = new DumperOptions();
    options.setDefaultFlowStyle(FlowStyle.BLOCK);
    options.setWidth(4096);

    return new Yaml(representer, options);
  }
}
