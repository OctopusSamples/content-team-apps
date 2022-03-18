package com.octopus.githubactions.domain.framework.configuration;

import com.fasterxml.jackson.databind.deser.std.DateDeserializers.TimestampDeserializer;
import com.github.jasminb.jsonapi.IntegerIdHandler;
import com.github.jasminb.jsonapi.StringIdHandler;
import com.octopus.githubactions.builders.dsl.Build;
import com.octopus.githubactions.builders.dsl.Jobs;
import com.octopus.githubactions.builders.dsl.On;
import com.octopus.githubactions.builders.dsl.Push;
import com.octopus.githubactions.builders.dsl.RunStep;
import com.octopus.githubactions.builders.dsl.UsesWith;
import com.octopus.githubactions.builders.dsl.Workflow;
import com.octopus.githubactions.builders.dsl.WorkflowDispatch;
import com.octopus.githubactions.domain.entities.Audit;
import com.octopus.githubactions.domain.entities.GithubUserLoggedInForFreeToolsEventV1;
import io.quarkus.runtime.annotations.RegisterForReflection;

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
    Audit.class,
    GithubUserLoggedInForFreeToolsEventV1.class,
    IntegerIdHandler.class,
    StringIdHandler.class})
public class ReflectionConfig {

}
