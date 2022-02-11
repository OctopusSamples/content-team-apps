package com.octopus.githubactions.configuration;

import com.fasterxml.jackson.databind.deser.std.DateDeserializers.TimestampDeserializer;
import io.quarkus.runtime.annotations.RegisterForReflection;
import com.octopus.githubactions.builders.dsl.Build;
import com.octopus.githubactions.builders.dsl.Jobs;
import com.octopus.githubactions.builders.dsl.On;
import com.octopus.githubactions.builders.dsl.Push;
import com.octopus.githubactions.builders.dsl.RunStep;
import com.octopus.githubactions.builders.dsl.UsesWith;
import com.octopus.githubactions.builders.dsl.Workflow;
import com.octopus.githubactions.builders.dsl.WorkflowDispatch;
import org.joda.time.Interval;

/**
 * An empty class used to list classes that we need to expose for reflection.
 */
@RegisterForReflection(targets = {
    Build.class,
    Jobs.class,
    On.class,
    Push.class,
    RunStep.class,
    UsesWith.class,
    Workflow.class,
    WorkflowDispatch.class,
    TimestampDeserializer.class,
    Audit.class})
public class ReflectionConfig {

}
