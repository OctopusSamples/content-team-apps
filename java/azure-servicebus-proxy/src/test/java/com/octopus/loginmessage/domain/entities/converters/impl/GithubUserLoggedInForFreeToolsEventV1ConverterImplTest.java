package com.octopus.loginmessage.domain.entities.converters.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.octopus.loginmessage.domain.entities.GithubUserLoggedInForFreeToolsEventV1;
import com.octopus.loginmessage.domain.entities.GithubUserLoggedInForFreeToolsEventV1Upstream;
import org.junit.jupiter.api.Test;

public class GithubUserLoggedInForFreeToolsEventV1ConverterImplTest {
  private static final GithubUserLoggedInForFreeToolsEventV1ConverterImpl GITHUB_USER_LOGGED_IN_FOR_FREE_TOOLS_EVENT_V_1_CONVERTER =
      new GithubUserLoggedInForFreeToolsEventV1ConverterImpl();

  @Test
  public void testConversion() {
    final GithubUserLoggedInForFreeToolsEventV1Upstream upstream =
        GITHUB_USER_LOGGED_IN_FOR_FREE_TOOLS_EVENT_V_1_CONVERTER
            .from(GithubUserLoggedInForFreeToolsEventV1.builder()
                .emailAddress("email")
                .firstName("firstname")
                .lastName("lastname")
                .utmParameters(ImmutableMap.<String, String>builder()
                    .put("key", "value")
                    .build())
                .programmingLanguage("language")
                .build());

    assertEquals("email", upstream.getEmailAddress());
    assertEquals("firstname", upstream.getFirstName());
    assertEquals("lastname", upstream.getLastName());
    assertEquals("value", upstream.getUtmParameters().get("key"));
    assertEquals("language", upstream.getProgrammingLanguage());
  }
}
