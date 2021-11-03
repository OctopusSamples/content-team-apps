package com.octopus.githubactions.configuration;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * An empty class used to list classes that we need to expose for reflection.
 */
@RegisterForReflection(targets = {Build.class, Jobs.class, On.class, Push.class, RunStep.class,
    UsesWith.class, Workflow.class, WorkflowDispatch.class})
public class ReflectionConfig {

}
