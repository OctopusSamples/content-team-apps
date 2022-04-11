package com.octopus.githubrepo.domain.framework.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.TimestampDeserializer;
import com.github.jasminb.jsonapi.IntegerIdHandler;
import com.github.jasminb.jsonapi.Link;
import com.github.jasminb.jsonapi.Links;
import com.github.jasminb.jsonapi.StringIdHandler;
import com.octopus.githubrepo.domain.entities.CreateGithubCommit;
import com.octopus.githubrepo.domain.entities.GenerateTemplate;
import com.octopus.githubrepo.domain.entities.Health;
import com.octopus.githubrepo.domain.entities.PopulateGithubRepo;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.kohsuke.github.GHBlob;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHPerson;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeBuilder;
import org.kohsuke.github.GHTreeEntry;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.extras.okhttp3.OkHttpGitHubConnector;

/**
 * This class is used to configure which other classes must be included in the native image intact.
 * Otherwise, the native compilation will strip out unreferenced methods, which can cause issues with
 * reflection.
 *
 * <p>If there are any weird null pointer exceptions when working with the GitHub client that only
 * appear in a native build, there is a good chance the serialization classes involved need to be
 * listed here.
 */
@RegisterForReflection(
    targets = {StringIdHandler.class,
        PopulateGithubRepo.class,
        CreateGithubCommit.class,
        Health.class,
        GenerateTemplate.class,
        IntegerIdHandler.class,
        SerializationFeature.class,
        TimestampDeserializer.class,
        Link.class,
        Links.class,
        OkHttpGitHubConnector.class,
        GHRepository.class,
        GHBranch.class,
        GHUser.class,
        GHBlob.class,
        GHTree.class,
        GHPerson.class,
        GHCommit.class,
        GHTreeBuilder.class,
        GHTreeEntry.class,
        GHRef.class},
    classNames = {"org.kohsuke.github.GitHubInteractiveObject"},
    ignoreNested = false)
public class MyReflectionConfiguration {

}
