package com.octopus.audits.domain.configuration;

import com.fasterxml.jackson.databind.deser.std.DateDeserializers.TimestampDeserializer;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * An empty class used to list classes that we need to expose for reflection.
 */
@RegisterForReflection(targets = {TimestampDeserializer.class})
public class ReflectionConfig {

}
