package com.octopus.githubrepo.domain.handlers.populaterepo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import com.octopus.githubrepo.domain.entities.github.GitHubCommit;
import com.octopus.githubrepo.domain.entities.github.GitHubPublicKey;
import com.octopus.githubrepo.domain.entities.github.GitHubUser;
import com.octopus.githubrepo.infrastructure.clients.GitHubClient;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommitBuilder;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeBuilder;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.connector.GitHubConnector;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

public class BaseGitHubTest extends BaseTest {

  /**
   * A third party GitHub client is used to push files to a GitHub repo. This method stubs out
   * all the interactions used by this service.
   * @param gitHubBuilder The GitHub builder.
   */
  protected void mockGithubClient(final GitHubBuilder gitHubBuilder) throws IOException {
    Mockito.when(gitHubBuilder.withOAuthToken(any())).thenReturn(gitHubBuilder);
    Mockito.when(gitHubBuilder.withConnector(ArgumentMatchers.<GitHubConnector>any()))
        .thenReturn(gitHubBuilder);

    final GitHub gitHub = Mockito.mock(GitHub.class);
    final GHRepository repo = Mockito.mock(GHRepository.class);
    final GHTreeBuilder treeBuilder = Mockito.mock(GHTreeBuilder.class);
    final GHCommitBuilder commitBuilder = Mockito.mock(GHCommitBuilder.class);
    final GHCommit commit = Mockito.mock(GHCommit.class);
    final GHRef ref = Mockito.mock(GHRef.class);

    doNothing().when(ref).updateTo(any());
    Mockito.when(commitBuilder.tree(any())).thenReturn(commitBuilder);
    Mockito.when(commitBuilder.parent(any())).thenReturn(commitBuilder);
    Mockito.when(commitBuilder.message(any())).thenReturn(commitBuilder);
    Mockito.when(commitBuilder.create()).thenReturn(commit);
    Mockito.when(treeBuilder.baseTree(any())).thenReturn(treeBuilder);
    Mockito.when(treeBuilder.create()).thenReturn(Mockito.mock(GHTree.class));
    Mockito.when(treeBuilder.add(anyString(), any(byte[].class), anyBoolean()))
        .thenReturn(treeBuilder);
    Mockito.when(repo.createTree()).thenReturn(treeBuilder);
    Mockito.when(repo.createCommit()).thenReturn(commitBuilder);
    Mockito.when(repo.getRef(any())).thenReturn(ref);
    Mockito.when(repo.getBranch(any())).thenReturn(Mockito.mock(GHBranch.class));
    Mockito.when(gitHub.getRepository(any())).thenReturn(repo);

    Mockito.when(gitHubBuilder.build()).thenReturn(gitHub);
  }

  /**
   * Mocks the github client for various scenarios.
   * @param gitHubClient The client to mock.
   * @param repoExists true if the repo should be found to exist, and false otherwise
   * @param linksExist true if the list of shas should return a paged response, and false otherwise
   */
  protected void mockGithubClient(final GitHubClient gitHubClient, final boolean repoExists, final boolean linksExist) {

    final Response notFoundResponse = Mockito.mock(Response.class);
    Mockito.when(notFoundResponse.getStatus()).thenReturn(404);

    final Response foundResponse = Mockito.mock(Response.class);
    Mockito.when(foundResponse.getStatus()).thenReturn(200);

    final Response mockScopeResponse = Mockito.mock(Response.class);
    Mockito.when(mockScopeResponse.getHeaderString("X-OAuth-Scopes")).thenReturn("workflow,repo");

    final ClientWebApplicationException notFoundException = Mockito.mock(
        ClientWebApplicationException.class);
    Mockito.when(notFoundException.getResponse()).thenReturn(notFoundResponse);

    final Response linksResponse = Mockito.mock(Response.class);

    if (linksExist) {
      Mockito.when(linksResponse.getHeaderString(any()))
          .thenReturn("Link: <https://api.github.com/repos?page=3&per_page=100>; rel=\"next\",\n"
              + "<https://api.github.com/repos?page=50&per_page=100>; rel=\"last\"");
    } else {
      Mockito.when(linksResponse.getHeaderString(any())).thenReturn("");
    }

    Mockito.when(gitHubClient.checkRateLimit(any())).thenReturn(mockScopeResponse);
    Mockito.when(gitHubClient.getSecret(any(), any(), any(), any())).thenAnswer((InvocationOnMock invocation) -> {
      final String secretName = invocation.getArgument(3);

      // indicate that a secret to be preserved already exists
      if ("preserveme".equals(secretName)) {
        return foundResponse;
      }

      // All others can be found to not exist
      return notFoundResponse;
    });
    doThrow(notFoundException).when(gitHubClient).getFile(any(), any(), any(), any(), any());
    doNothing().when(gitHubClient).createFile(any(), any(), any(), any(), any());
    Mockito.when(gitHubClient.getCommitsRaw(any(), any(), anyInt(), any()))
        .thenReturn(linksResponse);
    Mockito.when(gitHubClient.getCommits(any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(List.of(
            GitHubCommit.builder().sha("sha12345").build()));
    Mockito.when(gitHubClient.getBranch(any(), any(), any(), any())).thenThrow(notFoundException);
    if (repoExists) {
      Mockito.when(gitHubClient.getRepo(any(), any(), any())).thenReturn(foundResponse);
    } else {
      Mockito.when(gitHubClient.getRepo(any(), any(), any())).thenThrow(notFoundException);
    }
    Mockito.when(gitHubClient.getUser(any()))
        .thenReturn(GitHubUser.builder().login("testuser").build());
    Mockito.when(gitHubClient.getPublicKey(any(), any(), any()))
        .thenReturn(GitHubPublicKey.builder().key("test").keyId("test").build());
  }
}
