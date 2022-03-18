package com.octopus.githubrepo.domain.utils.impl;

import com.google.common.hash.Hashing;
import com.octopus.jwt.JwtInspector;
import com.octopus.githubrepo.domain.utils.OctopusLoginUtils;
import com.octopus.githubrepo.infrastructure.clients.OctopusClient;
import io.vavr.control.Try;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

/**
 * An implementation of OctopusLoginUtils.
 */
@ApplicationScoped
public class OctopusLoginUtilsImpl implements OctopusLoginUtils {
  @Inject
  JwtInspector jwtInspector;

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<String> getCsrf(final List<String> cookieHeaders) {
    return cookieHeaders
        .stream()
        .filter(c -> c.startsWith("Octopus-Csrf-Token"))
        .filter(c -> c.split("=").length == 2)
        .map(c -> c.split("=")[1])
        .findFirst();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getCookies(final Response response) {
    return response
        .getHeaders()
        .entrySet()
        .stream()
        .filter(e -> e.getKey().equalsIgnoreCase("set-cookie"))
        .flatMap(e -> e.getValue().stream().map(Object::toString))
        .map(c -> c.split(";")[0])
        .filter(c -> c.split("=")[0].startsWith("Octopus"))
        .collect(Collectors.toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getStateHash(final String state) {
    final byte[] hash = Hashing.sha256()
        .hashBytes(("OctoState" + state).getBytes(StandardCharsets.UTF_8)).asBytes();
    final String base64encoded = Base64.getEncoder().encodeToString(hash);
    return Try.of(() -> URLEncoder.encode(base64encoded, StandardCharsets.UTF_8.toString()))
        .getOrElse("");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getNonceHash(final String idToken) {
    return jwtInspector.getClaim(idToken, "nonce")
        .map(n -> Hashing.sha256()
            .hashBytes(("OctoNonce" + n).getBytes(StandardCharsets.UTF_8)).asBytes())
        .map(s -> Base64.getEncoder().encodeToString(s))
        .map(b -> Try.of(() -> URLEncoder.encode(b, StandardCharsets.UTF_8.toString()))
            .getOrElse(""))
        .orElse("");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Response logIn(final URI apiUri, final String idToken, final String state,
      final String stateHash, final String nonceHash) {
    final OctopusClient remoteApi = RestClientBuilder.newBuilder()
        .baseUri(apiUri)
        .build(OctopusClient.class);
    return remoteApi.logIn(idToken, state, "s=" + stateHash + ";n=" + nonceHash);
  }
}
