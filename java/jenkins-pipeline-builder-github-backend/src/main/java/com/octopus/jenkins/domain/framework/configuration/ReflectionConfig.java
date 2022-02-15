package com.octopus.jenkins.domain.framework.configuration;

import com.fasterxml.jackson.databind.deser.std.DateDeserializers.TimestampDeserializer;
import com.github.jasminb.jsonapi.IntegerIdHandler;
import com.github.jasminb.jsonapi.StringIdHandler;
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
    IntegerIdHandler.class,
    StringIdHandler.class})
public class ReflectionConfig {

}
