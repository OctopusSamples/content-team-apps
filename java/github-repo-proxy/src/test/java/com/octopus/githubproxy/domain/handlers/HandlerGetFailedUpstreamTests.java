package com.octopus.githubproxy.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

/**
 * These tests are  focused on the retrieval of resources through GET operations, but where the
 * upstream API called failed.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandlerGetFailedUpstreamTests {

  @Inject
  ResourceHandler handler;

  @InjectMock
  @RestClient
  GitHubClient gitHubClient;

  @BeforeEach
  public void setup() {
    final Response errorResponse = Mockito.mock(Response.class);
    Mockito.when(errorResponse.getStatus()).thenReturn(500);
    Mockito.when(errorResponse.getStatusInfo()).thenReturn(Mockito.mock(StatusType.class));
    doThrow(new ClientWebApplicationException(errorResponse)).when(gitHubClient)
        .getRepo(any(), any(), any());
  }

  @Test
  public void getResource() {
    assertThrows(ClientWebApplicationException.class,
        () -> handler.getOne(
            "owner/repo",
            List.of("main"),
            null,
            null,
            ""));
  }
}
