package com.octopus.oauth.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.features.OauthClientCreds;
import com.octopus.oauth.Oauth;
import com.octopus.oauth.OauthClientCredsAccessor;
import io.vavr.control.Try;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

public class OauthClientCredsAccessorImplTest {

  @Test
  public void verifyNullHandling() {
    assertThrows(NullPointerException.class, () -> new OauthClientCredsAccessorImpl(
        null,
        (authorization, grantType, clientId, scope) -> null));
    assertThrows(NullPointerException.class, () -> new OauthClientCredsAccessorImpl(
        new OauthClientCreds() {
          @Override
          public Optional<String> clientId() {
            return Optional.empty();
          }

          @Override
          public Optional<String> clientSecret() {
            return Optional.empty();
          }
        },
        null));
    assertThrows(NullPointerException.class, () -> new OauthClientCredsAccessorImpl(
        new OauthClientCreds() {
          @Override
          public Optional<String> clientId() {
            return Optional.empty();
          }

          @Override
          public Optional<String> clientSecret() {
            return Optional.empty();
          }
        },
        (authorization, grantType, clientId, scope) -> null)
        .getAccessToken(null));
  }

  @Test
  public void testAccessTokenCaching() {
    final OauthClientCredsAccessor oauthClientCredsAccessor = new OauthClientCredsAccessorImpl(
        new OauthClientCreds() {
          @Override
          public Optional<String> clientId() {
            return Optional.of("clientid");
          }

          @Override
          public Optional<String> clientSecret() {
            return Optional.of("secret");
          }
        },
        (authorization, grantType, clientId, scope) -> {
          final Oauth oAuth = new Oauth();
          oAuth.setAccessToken(RandomStringUtils.random(10, true, true));
          oAuth.setExpiresIn(Integer.MAX_VALUE);
          return oAuth;
        });


    final Try<String> accessToken = oauthClientCredsAccessor.getAccessToken("scope");
    final Try<String> accessToken2 = oauthClientCredsAccessor.getAccessToken("scope");

    assertEquals(accessToken.get(), accessToken2.get());
  }

  @Test
  public void testAccessTokenCacheInvalidation() {
    final OauthClientCredsAccessor oauthClientCredsAccessor = new OauthClientCredsAccessorImpl(
        new OauthClientCreds() {
          @Override
          public Optional<String> clientId() {
            return Optional.of("clientid");
          }

          @Override
          public Optional<String> clientSecret() {
            return Optional.of("secret");
          }
        },
        (authorization, grantType, clientId, scope) -> {
          final Oauth oAuth = new Oauth();
          oAuth.setAccessToken(RandomStringUtils.random(10, true, true));
          oAuth.setExpiresIn(Integer.MIN_VALUE);
          return oAuth;
        });


    final Try<String> accessToken = oauthClientCredsAccessor.getAccessToken("scope");
    final Try<String> accessToken2 = oauthClientCredsAccessor.getAccessToken("scope");

    assertNotEquals(accessToken.get(), accessToken2.get());
  }

  @Test
  public void testFailedResponse() {
    final OauthClientCredsAccessor oauthClientCredsAccessor = new OauthClientCredsAccessorImpl(
        new OauthClientCreds() {
          @Override
          public Optional<String> clientId() {
            return Optional.of("clientid");
          }

          @Override
          public Optional<String> clientSecret() {
            return Optional.of("secret");
          }
        },
        (authorization, grantType, clientId, scope) -> {
          throw new RuntimeException();
        });

    assertTrue(oauthClientCredsAccessor.getAccessToken("scope").isFailure());
  }

  @Test
  public void testBlankAccessToken() {
    final OauthClientCredsAccessor oauthClientCredsAccessor = new OauthClientCredsAccessorImpl(
        new OauthClientCreds() {
          @Override
          public Optional<String> clientId() {
            return Optional.of("clientid");
          }

          @Override
          public Optional<String> clientSecret() {
            return Optional.of("secret");
          }
        },
        (authorization, grantType, clientId, scope) -> {
          final Oauth oAuth = new Oauth();
          oAuth.setAccessToken(" ");
          oAuth.setExpiresIn(Integer.MAX_VALUE);
          return oAuth;
        });

    assertTrue(oauthClientCredsAccessor.getAccessToken("scope").isFailure());
  }

  @Test
  public void testEmptyClientId() {
    final OauthClientCredsAccessor oauthClientCredsAccessor = new OauthClientCredsAccessorImpl(
        new OauthClientCreds() {
          @Override
          public Optional<String> clientId() {
            return Optional.empty();
          }

          @Override
          public Optional<String> clientSecret() {
            return Optional.of("secret");
          }
        },
        (authorization, grantType, clientId, scope) -> {
          throw new RuntimeException();
        });

    assertTrue(oauthClientCredsAccessor.getAccessToken("scope").isFailure());
  }

  @Test
  public void testEmptyClientSecret() {
    final OauthClientCredsAccessor oauthClientCredsAccessor = new OauthClientCredsAccessorImpl(
        new OauthClientCreds() {
          @Override
          public Optional<String> clientId() {
            return Optional.of("clientId");
          }

          @Override
          public Optional<String> clientSecret() {
            return Optional.empty();
          }
        },
        (authorization, grantType, clientId, scope) -> {
          throw new RuntimeException();
        });

    assertTrue(oauthClientCredsAccessor.getAccessToken("scope").isFailure());
  }
}
