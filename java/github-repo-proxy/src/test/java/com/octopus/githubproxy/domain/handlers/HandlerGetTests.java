package com.octopus.githubproxy.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.encryption.CryptoUtils;
import com.octopus.exceptions.EntityNotFoundException;
import com.octopus.githubproxy.TestingProfile;
import com.octopus.githubproxy.domain.entities.GitHubRepo;
import com.octopus.githubproxy.domain.entities.Repo;
import com.octopus.githubproxy.domain.entities.RepoOwner;
import com.octopus.githubproxy.infrastructure.clients.GitHubClient;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

/**
 * These tests are  focused on the retrieval of resources through GET operations.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class HandlerGetTests {

  @Inject
  ResourceHandler handler;

  @InjectMock
  @RestClient
  GitHubClient gitHubClient;

  @InjectMock
  CryptoUtils cryptoUtils;

  @Inject
  ResourceConverter resourceConverter;

  @BeforeEach
  public void setup() {
    final Response missingResponse = Mockito.mock(Response.class);
    Mockito.when(missingResponse.getStatus()).thenReturn(404);
    Mockito.when(missingResponse.getStatusInfo()).thenReturn(Mockito.mock(StatusType.class));

    // Mock the response from the GitHub API to either return a repo or a 404
    Mockito.when(gitHubClient.getRepo(any(), any(), any())).thenAnswer(invocation -> {
      final String owner = invocation.getArgument(0, String.class);
      final String repo = invocation.getArgument(1, String.class);

      if ("owner".equals(owner) && "repo".equals(repo)) {
        return Repo
            .builder()
            .owner(RepoOwner.builder().login("owner").build())
            .name("repo").build();
      }

      throw new ClientWebApplicationException(missingResponse);
    });

    Mockito.when(cryptoUtils.decrypt(any(), any(), any())).thenReturn("decrypted");
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

    assertThrows(EntityNotFoundException.class, () ->
        handler.getOne(
            "https://api.github.com/repos/blah/blah",
            List.of("main"),
            null,
            null,
            "")
    );
  }

  @Test
  public void getResource() throws DocumentSerializationException {
    final String result = handler.getOne(
        "https://api.github.com/repos/owner/repo",
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
