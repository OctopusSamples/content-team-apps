package com.octopus.githubproxy.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.exceptions.EntityNotFoundException;
import com.octopus.githubproxy.domain.entities.GitHubRepo;
import com.octopus.githubproxy.domain.entities.Repo;
import com.octopus.githubproxy.domain.entities.RepoOwner;
import com.octopus.githubproxy.infrastructure.clients.GitHubClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

/**
 * These tests are mostly focused on the retrieval of new resources through GET operations.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandlerGetTests {

  @Inject
  ResourceHandler handler;

  @InjectMock
  @RestClient
  GitHubClient gitHubClient;

  @Inject
  ResourceConverter resourceConverter;

  @BeforeEach
  public void setup() {
    Mockito.when(gitHubClient.getRepo(any(), any(), any())).thenReturn(
        Repo
            .builder()
            .name("repo")
            .owner(RepoOwner.builder().login("owner").build())
            .build());
  }

  @Test
  public void getOneResourceTestNull() {
    assertThrows(NullPointerException.class, () -> {
      handler.getOne(
          null,
          List.of("testing"),
          null,
          null,
          "");
    });

    assertThrows(NullPointerException.class, () -> {
      handler.getOne(
          "1",
          null,
          null,
          null,
          "");
    });

    assertThrows(NullPointerException.class, () -> {
      handler.getOne(
          "1",
          List.of("testing"),
          null,
          null,
          null);
    });
  }

  @Test
  public void getMissingResource() {
    assertThrows(EntityNotFoundException.class, () ->
      handler.getOne(
          "1000000000000000000",
          List.of("main"),
          null,
          null,
          "")
    );
  }

  @Test
  public void getResource() throws DocumentSerializationException {
    final String result = handler.getOne(
        "owner/repo",
        List.of("main"),
        null,
        null,
        "");

    final JSONAPIDocument<GitHubRepo> document = resourceConverter.readDocument(
        result.getBytes(StandardCharsets.UTF_8),
        GitHubRepo.class);

    assertEquals("owner", document.get().getOwner());
    assertEquals("repo", document.get().getRepo());
  }
}
