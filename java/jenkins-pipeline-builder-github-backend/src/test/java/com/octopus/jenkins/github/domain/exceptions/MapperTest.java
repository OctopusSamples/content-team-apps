package com.octopus.jenkins.github.domain.exceptions;

import com.octopus.jenkins.github.domain.TestingProfile;
import com.octopus.jenkins.github.domain.framework.providers.BadRequestMapper;
import com.octopus.jenkins.github.domain.framework.providers.EntityNotFoundMapper;
import com.octopus.jenkins.github.domain.framework.providers.ServerErrorMapper;
import com.octopus.jenkins.github.domain.framework.providers.UnauthorizedMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(TestingProfile.class)
public class MapperTest {

  @Inject
  UnauthorizedMapper unauthorizedMapper;

  @Inject
  EntityNotFoundMapper entityNotFoundMapper;

  @Inject
  ServerErrorMapper serverErrorMapper;

  @Inject
  BadRequestMapper badRequestMapper;


  @Test
  public void verifyUnauthorizedMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      unauthorizedMapper.toResponse(null);
    });
  }

  @Test
  public void verifyUnauthorizedMapperResponse() {
    final Response response = unauthorizedMapper.toResponse(
        new Unauthorized());
    Assertions.assertEquals(401, response.getStatus());
  }

  @Test
  public void verifyEntityNotFoundMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      entityNotFoundMapper.toResponse(null);
    });
  }

  @Test
  public void verifyEntityNotFoundMapperResponse() {
    final Response response = entityNotFoundMapper.toResponse(
        new EntityNotFound());
    Assertions.assertEquals(404, response.getStatus());
  }

  @Test
  public void verifyServerErrorMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      serverErrorMapper.toResponse(null);
    });
  }

  @Test
  public void verifyServerErrorMapperResponse() {
    final Response response = serverErrorMapper.toResponse(
        new ServerError());
    Assertions.assertEquals(500, response.getStatus());
  }

  @Test
  public void verifyBadRequestMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      badRequestMapper.toResponse(null);
    });
  }

  @Test
  public void verifyBadRequestMapperResponse() {
    final Response response = badRequestMapper.toResponse(
        new BadRequest());
    Assertions.assertEquals(400, response.getStatus());
  }
}
