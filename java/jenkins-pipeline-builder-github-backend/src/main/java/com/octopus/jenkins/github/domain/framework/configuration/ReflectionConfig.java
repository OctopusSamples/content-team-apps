package com.octopus.jenkins.github.domain.framework.configuration;

import com.fasterxml.jackson.databind.deser.std.DateDeserializers.TimestampDeserializer;
import com.github.jasminb.jsonapi.IntegerIdHandler;
import com.github.jasminb.jsonapi.StringIdHandler;
import com.octopus.jenkins.github.domain.entities.Audit;
import com.octopus.jenkins.github.domain.entities.GithubUserLoggedInForFreeToolsEventV1;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * An empty class used to list classes that we need to expose for reflection.
 */
@RegisterForReflection(targets = {
    TimestampDeserializer.class,
    Audit.class,
    IntegerIdHandler.class,
    StringIdHandler.class,
    GithubUserLoggedInForFreeToolsEventV1.class})
public class ReflectionConfig {

}
