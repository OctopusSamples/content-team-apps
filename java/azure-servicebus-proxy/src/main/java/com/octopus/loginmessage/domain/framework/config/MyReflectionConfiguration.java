package com.octopus.loginmessage.domain.framework.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.TimestampDeserializer;
import com.github.jasminb.jsonapi.IntegerIdHandler;
import com.github.jasminb.jsonapi.Link;
import com.github.jasminb.jsonapi.Links;
import com.github.jasminb.jsonapi.StringIdHandler;
import com.octopus.loginmessage.domain.entities.GithubUserLoggedInForFreeToolsEventV1;
import com.octopus.loginmessage.domain.entities.Health;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * This class is used to configure which other classes must be included in the native image intact.
 * Otherwise the native compilation will strip out unreferenced methods, which can cause issues with
 * reflection.
 */
@RegisterForReflection(
    targets = {StringIdHandler.class,
        GithubUserLoggedInForFreeToolsEventV1.class,
        Health.class,
        IntegerIdHandler.class,
        SerializationFeature.class,
        TimestampDeserializer.class,
        Link.class,
        Links.class},
    ignoreNested = false)
public class MyReflectionConfiguration {

}
