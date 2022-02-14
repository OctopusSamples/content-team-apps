package com.octopus.audits.domain.configuration;

import com.fasterxml.jackson.databind.deser.std.DateDeserializers.TimestampDeserializer;
import com.github.jasminb.jsonapi.Link;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * An empty class used to list classes that we need to expose for reflection.
 */
@RegisterForReflection(
    targets = {TimestampDeserializer.class, Link.class},
    classNames = {"com.github.jasminb.jsonapi.Link.LinkSerializer"})
public class ReflectionConfig {

}
