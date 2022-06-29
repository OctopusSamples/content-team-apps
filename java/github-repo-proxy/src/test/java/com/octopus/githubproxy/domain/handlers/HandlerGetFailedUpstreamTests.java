package com.octopus.githubproxy.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

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
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
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
 * These tests are  focused on the retrieval of resources through GET operations, but where the upstream API called failed.
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class HandlerGetFailedUpstreamTests {

  @Inject
  ResourceHandler handler;

  @InjectMock
  @RestClient
  GitHubClient gitHubClient;

  @InjectMock
  CryptoUtils cryptoUtils;

  @BeforeEach
  public void setup() {
    final Response errorResponse = Mockito.mock(Response.class);
    Mockito.when(errorResponse.getStatus()).thenReturn(500);
    Mockito.when(errorResponse.getStatusInfo()).thenReturn(Mockito.mock(StatusType.class));
    Mockito.when(gitHubClient.getRepo(any(), any(), any())).thenReturn(Uni.createFrom()
        .item(Repo.builder().build())
        .invoke(Unchecked.consumer(r -> {
          throw new ClientWebApplicationException(errorResponse);
        })));
    Mockito.when(cryptoUtils.decrypt(any(), any(), any())).thenReturn("decrypted");
  }

  @Test
  public void getResource() {
    handler.getOne(
            "https://api.github.com/repos/owner/repo",
            List.of("main"),
            null,
            null,
            "")
        .onFailure().invoke(e -> assertTrue(e instanceof ClientWebApplicationException))
        .subscribe().with(s -> fail());
  }
}
