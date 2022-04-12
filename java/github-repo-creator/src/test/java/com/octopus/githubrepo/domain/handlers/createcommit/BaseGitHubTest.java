package com.octopus.githubrepo.domain.handlers.createcommit;

import static org.mockito.ArgumentMatchers.any;

import com.octopus.githubrepo.domain.entities.github.GitHubUser;
import com.octopus.githubrepo.infrastructure.clients.GitHubClient;
import javax.ws.rs.core.Response;
import org.mockito.Mockito;

public class BaseGitHubTest extends BaseTest {
  protected void mockGitHubClient(final GitHubClient gitHubClient) {
    final Response foundResponse = Mockito.mock(Response.class);
    Mockito.when(foundResponse.getStatus()).thenReturn(200);

    final Response mockScopeResponse = Mockito.mock(Response.class);
    Mockito.when(mockScopeResponse.getHeaderString("X-OAuth-Scopes")).thenReturn("workflow,repo");

    Mockito.when(gitHubClient.getRepo(any(), any(), any())).thenReturn(foundResponse);
    Mockito.when(gitHubClient.checkRateLimit(any())).thenReturn(mockScopeResponse);
    Mockito.when(gitHubClient.getUser(any()))
        .thenReturn(GitHubUser.builder().login("testuser").build());
  }
}
