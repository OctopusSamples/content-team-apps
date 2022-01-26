package com.octopus.githuboauth.configuration;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.joda.time.Interval;

/**
 * An empty class used to list classes that we need to expose for reflection.
 */
@RegisterForReflection(targets = {Interval.class})
public class ReflectionConfig {

}
