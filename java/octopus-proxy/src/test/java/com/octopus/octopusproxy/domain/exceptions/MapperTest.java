package com.octopus.octopusproxy.domain.exceptions;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.github.jasminb.jsonapi.exceptions.InvalidJsonApiResourceException;
import com.octopus.exceptions.EntityNotFoundException;
import com.octopus.exceptions.InvalidAcceptHeadersException;
import com.octopus.exceptions.InvalidInputException;
import com.octopus.octopusproxy.domain.framework.providers.DocumentSerializationExceptionMapper;
import com.octopus.octopusproxy.domain.framework.providers.EntityNotFoundExceptionMapper;
import com.octopus.octopusproxy.domain.framework.providers.InvalidAcceptHeadersExceptionMapper;
import com.octopus.octopusproxy.domain.framework.providers.InvalidInputExceptionExceptionMapper;
import com.octopus.octopusproxy.domain.framework.providers.InvalidJsonApiResourceExceptionMapper;
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
  InvalidAcceptHeadersExceptionMapper InvalidAcceptHeadersExceptionMapper;
  
  @Inject
  EntityNotFoundExceptionMapper EntityNotFoundExceptionMapperException;

  @Inject
  InvalidInputExceptionExceptionMapper InvalidInputExceptionExceptionMapper;

  @Test
  public void verifyInvalidAcceptHeadersExceptionMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      InvalidAcceptHeadersExceptionMapper.toResponse(null);
    });
  }

  @Test
  public void verifyInvalidAcceptHeadersExceptionMapperResponse() {
    final Response response = InvalidAcceptHeadersExceptionMapper.toResponse(
        new InvalidAcceptHeadersException());
    Assertions.assertEquals(406, response.getStatus());
  }

  @Test
  public void verifyEntityNotFoundExceptionMapperExceptionNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      new EntityNotFoundExceptionMapper().toResponse(null);
    });
  }

  @Test
  public void verifyEntityNotFoundExceptionMapperExceptionResponse() {
    final Response response = EntityNotFoundExceptionMapperException.toResponse(
        new EntityNotFoundException());
    Assertions.assertEquals(404, response.getStatus());
  }

  @Test
  public void verifyInvalidInputExceptionExceptionMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      InvalidInputExceptionExceptionMapper.toResponse(null);
    });
  }

  @Test
  public void verifyInvalidInputExceptionExceptionMapperResponse() {
    final Response response = InvalidInputExceptionExceptionMapper.toResponse(
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
