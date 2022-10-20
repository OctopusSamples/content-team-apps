package com.octopus.githubactions.shared.builders;

import com.octopus.githubactions.shared.builders.dsl.Build;
import com.octopus.githubactions.shared.builders.dsl.On;
import com.octopus.githubactions.shared.builders.dsl.RunStep;
import com.octopus.githubactions.shared.builders.dsl.UsesWith;
import com.octopus.githubactions.shared.builders.dsl.Workflow;
import com.octopus.githubactions.shared.builders.dsl.WorkflowDispatch;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Builds preconfigured instances of SnakeYAML.
 */
public final class SnakeYamlFactory {

  /**
   * A custom representer that ignores null entries and preserves the order of elements.
   */
  private static final class CustomRepresenter extends Representer {

    private static final List<String> PROPERTY_ORDER = List.of("runsOn", "steps", "id", "name",
        "on", "uses", "ifProperty", "env", "with", "run", "shell");

    private CustomRepresenter() {
      super();
      final PropertyUtils propUtil = new PropertyUtils() {
        @Override
        protected Set<Property> createPropertySet(final Class<? extends Object> type,
            final BeanAccess beanAccess) {
          return getPropertiesMap(type, beanAccess).values().stream().sequential()
              .filter(
                  prop -> prop.isReadable() && (isAllowReadOnlyProperties() || prop.isWritable()))
              .sorted((t1, t2) -> {
                if (StringUtils.equals(t1.getName(), t2.getName())) {
                  return 0;
                }

                if (PROPERTY_ORDER.contains(t1.getName()) && !PROPERTY_ORDER.contains(
                    t2.getName())) {
                  return -1;
                }

                if (!PROPERTY_ORDER.contains(t1.getName()) && PROPERTY_ORDER.contains(
                    t2.getName())) {
                  return 1;
                }

                if (!PROPERTY_ORDER.contains(t1.getName()) && !PROPERTY_ORDER.contains(
                    t2.getName())) {
                  return StringUtils.compare(t1.getName(), t2.getName());
                }

                return PROPERTY_ORDER.indexOf(t1.getName()) < PROPERTY_ORDER.indexOf(t2.getName())
                    ? -1 : 1;
              })
              .collect(Collectors.toCollection(LinkedHashSet::new));
        }
      };
      setPropertyUtils(propUtil);
    }

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
  }

  /**
   * Builds an instance of SnakeYAML configured to generate workflow DSLs.
   *
   * @return A configured instance of SnakeYAML.
   */
  public static Yaml getConfiguredYaml() {
    final Representer representer = new CustomRepresenter();

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
    runDesc.substituteProperty("working-directory", String.class, "getWorkingDirectory",
        "setWorkingDirectory");
    runDesc.setExcludes("workingDirectory");
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
