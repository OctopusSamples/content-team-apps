package com.octopus.githubrepo.domain.exceptions;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.github.jasminb.jsonapi.exceptions.InvalidJsonApiResourceException;
import com.octopus.exceptions.EntityNotFound;
import com.octopus.exceptions.InvalidAcceptHeaders;
import com.octopus.exceptions.InvalidInput;
import com.octopus.githubrepo.domain.framework.providers.DocumentSerializationExceptionMapper;
import com.octopus.githubrepo.domain.framework.providers.EntityNotFoundMapper;
import com.octopus.githubrepo.domain.framework.providers.InvalidAcceptHeadersMapper;
import com.octopus.githubrepo.domain.framework.providers.InvalidInputExceptionMapper;
import com.octopus.githubrepo.domain.framework.providers.InvalidJsonApiResourceExceptionMapper;
import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class MapperTest {

  @Inject
  DocumentSerializationExceptionMapper documentSerializationExceptionMapper;
  
  @Inject
  InvalidJsonApiResourceExceptionMapper invalidJsonApiResourceExceptionMapper;
  
  @Inject
  InvalidAcceptHeadersMapper invalidAcceptHeadersMapper;
  
  @Inject
  EntityNotFoundMapper entityNotFoundMapper;

  @Inject
  InvalidInputExceptionMapper invalidInputExceptionMapper;

  @Test
  public void verifyInvalidAcceptHeadersMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      invalidAcceptHeadersMapper.toResponse(null);
    });
  }

  @Test
  public void verifyInvalidAcceptHeadersMapperResponse() {
    final Response response = invalidAcceptHeadersMapper.toResponse(
        new InvalidAcceptHeaders());
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
        new EntityNotFound());
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
        new InvalidInput());
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
