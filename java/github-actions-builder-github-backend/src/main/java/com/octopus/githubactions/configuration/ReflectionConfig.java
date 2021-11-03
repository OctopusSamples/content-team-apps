package com.octopus.githubactions.configuration;

import io.quarkus.runtime.annotations.RegisterForReflection;
import com.octopus.githubactions.builders.dsl.*;

@RegisterForReflection(targets = {Build.class, Jobs.class, On.class, Push.class, RunStep.class, UsesWith.class, Workflow.class, WorkflowDispatch.class})
public class ReflectionConfig {

}
