package com.octopus.githubrepo.domain.utils.impl;

import static org.mockito.ArgumentMatchers.any;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.exceptions.InvalidInputException;
import com.octopus.githubrepo.TestingProfile;
import com.octopus.githubrepo.domain.utils.ScopeVerifier;
import com.octopus.githubrepo.infrastructure.clients.GitHubClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(TestingProfile.class)
public class ScopeVerifierImplMissingRepoTest {

  @RestClient
  @InjectMock
  GitHubClient gitHubClient;

  @Inject
  ScopeVerifier scopeVerifier;

  @BeforeEach
  public void setup() throws DocumentSerializationException {
    final Response response = Mockito.mock(Response.class);
    Mockito.when(response.getHeaderString("X-OAuth-Scopes")).thenReturn("workflow,b");
    Mockito.when(gitHubClient.checkRateLimit(any())).thenReturn(response);
  }

  @Test
  public void testScopes() {
    Assertions.assertThrows(InvalidInputException.class,
        () -> scopeVerifier.verifyScopes("blah"));
  }
}
