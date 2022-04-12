package com.octopus.githubrepo.domain.exceptions;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.github.jasminb.jsonapi.exceptions.InvalidJsonApiResourceException;
import com.octopus.exceptions.EntityNotFoundException;
import com.octopus.exceptions.InvalidAcceptHeadersException;
import com.octopus.exceptions.InvalidInputException;
import com.octopus.githubrepo.TestingProfile;
import com.octopus.githubrepo.domain.framework.providers.DocumentSerializationExceptionMapper;
import com.octopus.githubrepo.domain.framework.providers.EntityNotFoundMapper;
import com.octopus.githubrepo.domain.framework.providers.InvalidAcceptHeadersMapper;
import com.octopus.githubrepo.domain.framework.providers.InvalidInputExceptionMapper;
import com.octopus.githubrepo.domain.framework.providers.InvalidJsonApiResourceExceptionMapper;
import com.octopus.githubrepo.domain.framework.providers.ServerErrorExceptionMapper;
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
  DocumentSerializationExceptionMapper documentSerializationExceptionMapper;
  
  @Inject
  InvalidJsonApiResourceExceptionMapper invalidJsonApiResourceExceptionMapper;

  @Inject
  ServerErrorExceptionMapper serverErrorExceptionMapper;
  
  @Inject
  InvalidAcceptHeadersMapper invalidAcceptHeadersMapper;
  
  @Inject
  EntityNotFoundMapper entityNotFoundMapper;

  @Inject
  InvalidInputExceptionMapper invalidInputExceptionMapper;

  @Test
  public void verifyServerErrorExceptionMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      serverErrorExceptionMapper.toResponse(null);
    });
  }

  @Test
  public void verifyInvalidAcceptHeadersMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      invalidAcceptHeadersMapper.toResponse(null);
    });
  }

  @Test
  public void verifyInvalidAcceptHeadersMapperResponse() {
    final Response response = invalidAcceptHeadersMapper.toResponse(
        new InvalidAcceptHeadersException());
    Assertions.assertEquals(406, response.getStatus());
  }

  @Test
  public void verifyEntityNotFoundMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      new EntityNotFoundMapper().toResponse(null);
    });
  }

  @Test
  public void verifyEntityNotFoundMapperResponse() {
    final Response response = entityNotFoundMapper.toResponse(
        new EntityNotFoundException());
    Assertions.assertEquals(404, response.getStatus());
  }

  @Test
  public void verifyInvalidInputExceptionMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      invalidInputExceptionMapper.toResponse(null);
    });
  }

  @Test
  public void verifyInvalidInputExceptionMapperResponse() {
    final Response response = invalidInputExceptionMapper.toResponse(
        new InvalidInputException());
    Assertions.assertEquals(400, response.getStatus());
  }


  @Test
  public void verifyInvalidJsonApiResourceExceptionMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      invalidJsonApiResourceExceptionMapper.toResponse(null);
    });
  }

  @Test
  public void verifyInvalidJsonApiResourceExceptionMapperResponse() {
    final Response response = invalidJsonApiResourceExceptionMapper.toResponse(
        new InvalidJsonApiResourceException("Doh!"));
    Assertions.assertEquals(400, response.getStatus());
  }

  @Test
  public void verifyDocumentSerializationExceptionMapperMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      documentSerializationExceptionMapper.toResponse(null);
    });
  }

  @Test
  public void verifyDocumentSerializationExceptionMapperMapperResponse() {
    final Response response = documentSerializationExceptionMapper.toResponse(
        new DocumentSerializationException(new Exception("Doh!")));
    Assertions.assertEquals(500, response.getStatus());
  }
}
