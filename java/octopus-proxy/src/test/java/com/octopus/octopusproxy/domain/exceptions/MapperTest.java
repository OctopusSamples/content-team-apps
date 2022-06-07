package com.octopus.octopusproxy.domain.exceptions;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.github.jasminb.jsonapi.exceptions.InvalidJsonApiResourceException;
import com.octopus.exceptions.EncryptionException;
import com.octopus.exceptions.EntityNotFoundException;
import com.octopus.exceptions.InvalidAcceptHeadersException;
import com.octopus.exceptions.InvalidInputException;
import com.octopus.exceptions.JsonSerializationException;
import com.octopus.exceptions.ServerErrorException;
import com.octopus.exceptions.UnauthorizedException;
import com.octopus.octopusproxy.domain.framework.providers.DocumentSerializationExceptionMapper;
import com.octopus.octopusproxy.domain.framework.providers.EncryptionExceptionMapper;
import com.octopus.octopusproxy.domain.framework.providers.EntityNotFoundExceptionMapper;
import com.octopus.octopusproxy.domain.framework.providers.InvalidAcceptHeadersExceptionMapper;
import com.octopus.octopusproxy.domain.framework.providers.InvalidInputExceptionExceptionMapper;
import com.octopus.octopusproxy.domain.framework.providers.InvalidJsonApiResourceExceptionMapper;
import com.octopus.octopusproxy.domain.framework.providers.JsonSerializationExceptionMapper;
import com.octopus.octopusproxy.domain.framework.providers.ServerErrorExceptionMapper;
import com.octopus.octopusproxy.domain.framework.providers.UnauthorizedExceptionMapper;
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

  @Inject
  JsonSerializationExceptionMapper jsonSerializationExceptionMapper;

  @Inject
  EncryptionExceptionMapper encryptionExceptionMapper;

  @Inject
  UnauthorizedExceptionMapper unauthorizedExceptionMapper;

  @Inject
  ServerErrorExceptionMapper serverErrorExceptionMapper;

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
  public void verifyDocumentSerializationExceptionMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      documentSerializationExceptionMapper.toResponse(null);
    });
  }

  @Test
  public void verifyDocumentSerializationExceptionMapperResponse() {
    final Response response = documentSerializationExceptionMapper.toResponse(
        new DocumentSerializationException(new Exception("Doh!")));
    Assertions.assertEquals(500, response.getStatus());
  }

  @Test
  public void verifyJsonSerializationExceptionMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      jsonSerializationExceptionMapper.toResponse(null);
    });
  }

  @Test
  public void verifyJsonSerializationExceptionMapperResponse() {
    final Response response = jsonSerializationExceptionMapper.toResponse(
        new JsonSerializationException(new Exception("Doh!")));
    Assertions.assertEquals(500, response.getStatus());
  }

  @Test
  public void verifyEncryptionExceptionMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      encryptionExceptionMapper.toResponse(null);
    });
  }

  @Test
  public void verifyEncryptionExceptionMapperResponse() {
    final Response response = encryptionExceptionMapper.toResponse(
        new EncryptionException(new Exception("Doh!")));
    Assertions.assertEquals(400, response.getStatus());
  }

  @Test
  public void verifyUnauthorizedExceptionMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      unauthorizedExceptionMapper.toResponse(null);
    });
  }

  @Test
  public void verifyUnauthorizedExceptionMapperResponse() {
    final Response response = unauthorizedExceptionMapper.toResponse(
        new UnauthorizedException(new Exception("Doh!")));
    Assertions.assertEquals(401, response.getStatus());
  }

  @Test
  public void verifyServerErrorExceptionMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      serverErrorExceptionMapper.toResponse(null);
    });
  }

  @Test
  public void verifyServerErrorExceptionMapperResponse() {
    final Response response = serverErrorExceptionMapper.toResponse(
        new ServerErrorException(new Exception("Doh!")));
    Assertions.assertEquals(500, response.getStatus());
  }
}
