package com.octopus.customers.domain.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.github.jasminb.jsonapi.exceptions.InvalidJsonApiResourceException;
import com.octopus.customers.domain.framework.providers.DocumentSerializationExceptionMapper;
import com.octopus.customers.domain.framework.providers.EntityNotFoundMapper;
import com.octopus.customers.domain.framework.providers.InvalidAcceptHeadersMapper;
import com.octopus.customers.domain.framework.providers.InvalidInputExceptionMapper;
import com.octopus.customers.domain.framework.providers.InvalidJsonApiResourceExceptionMapper;
import com.octopus.customers.domain.framework.providers.RsqlParserExceptionMapper;
import com.octopus.exceptions.EntityNotFound;
import com.octopus.exceptions.InvalidAcceptHeaders;
import com.octopus.exceptions.InvalidInput;
import cz.jirutka.rsql.parser.RSQLParserException;
import io.quarkus.test.junit.QuarkusTest;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class MapperTest {

  @Test
  public void verifyInvalidAcceptHeadersMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      new InvalidAcceptHeadersMapper().toResponse(null);
    });
  }

  @Test
  public void verifyInvalidAcceptHeadersMapperResponse() {
    final Response response = new InvalidAcceptHeadersMapper().toResponse(
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
    final Response response = new EntityNotFoundMapper().toResponse(
        new EntityNotFound());
    Assertions.assertEquals(404, response.getStatus());
  }

  @Test
  public void verifyRsqlParserExceptionMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      new RsqlParserExceptionMapper().toResponse(null);
    });
  }

  @Test
  public void verifyRsqlParserExceptionMapperResponse() {
    final Response response = new RsqlParserExceptionMapper().toResponse(
        new RSQLParserException(new Exception("Doh!")));
    Assertions.assertEquals(400, response.getStatus());
  }

  @Test
  public void verifyInvalidInputExceptionMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      new InvalidInputExceptionMapper().toResponse(null);
    });
  }

  @Test
  public void verifyInvalidInputExceptionMapperResponse() {
    final Response response = new InvalidInputExceptionMapper().toResponse(
        new InvalidInput());
    Assertions.assertEquals(400, response.getStatus());
  }


  @Test
  public void verifyInvalidJsonApiResourceExceptionMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      new InvalidJsonApiResourceExceptionMapper().toResponse(null);
    });
  }

  @Test
  public void verifyInvalidJsonApiResourceExceptionMapperResponse() {
    final Response response = new InvalidJsonApiResourceExceptionMapper().toResponse(
        new InvalidJsonApiResourceException("Doh!"));
    Assertions.assertEquals(400, response.getStatus());
  }

  @Test
  public void verifyDocumentSerializationExceptionMapperMapperNullInputs() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      new DocumentSerializationExceptionMapper().toResponse(null);
    });
  }

  @Test
  public void verifyDocumentSerializationExceptionMapperMapperResponse() {
    final Response response = new DocumentSerializationExceptionMapper().toResponse(
        new DocumentSerializationException(new Exception("Doh!")));
    Assertions.assertEquals(500, response.getStatus());
  }
}
