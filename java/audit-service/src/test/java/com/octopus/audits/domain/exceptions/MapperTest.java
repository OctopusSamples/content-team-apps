package com.octopus.audits.domain.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.github.jasminb.jsonapi.exceptions.InvalidJsonApiResourceException;
import com.octopus.audits.domain.framework.providers.DocumentSerializationExceptionMapper;
import com.octopus.audits.domain.framework.providers.EntityNotFoundMapper;
import com.octopus.audits.domain.framework.providers.InvalidAcceptHeadersMapper;
import com.octopus.audits.domain.framework.providers.InvalidInputExceptionMapper;
import com.octopus.audits.domain.framework.providers.InvalidJsonApiResourceExceptionMapper;
import com.octopus.audits.domain.framework.providers.RsqlParserExceptionMapper;
import cz.jirutka.rsql.parser.RSQLParserException;
import io.quarkus.test.junit.QuarkusTest;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class MapperTest {

  @Test
  public void verifyInvalidAcceptHeadersMapperNullInputs() {
    assertThrows(NullPointerException.class, () -> {
      new InvalidAcceptHeadersMapper().toResponse(null);
    });
  }

  @Test
  public void verifyInvalidAcceptHeadersMapperResponse() {
    final Response response = new InvalidAcceptHeadersMapper().toResponse(
        new InvalidAcceptHeaders());
    assertEquals(406, response.getStatus());
  }

  @Test
  public void verifyEntityNotFoundMapperNullInputs() {
    assertThrows(NullPointerException.class, () -> {
      new EntityNotFoundMapper().toResponse(null);
    });
  }

  @Test
  public void verifyEntityNotFoundMapperResponse() {
    final Response response = new EntityNotFoundMapper().toResponse(
        new EntityNotFound());
    assertEquals(404, response.getStatus());
  }

  @Test
  public void verifyRsqlParserExceptionMapperNullInputs() {
    assertThrows(NullPointerException.class, () -> {
      new RsqlParserExceptionMapper().toResponse(null);
    });
  }

  @Test
  public void verifyRsqlParserExceptionMapperResponse() {
    final Response response = new RsqlParserExceptionMapper().toResponse(
        new RSQLParserException(new Exception("Doh!")));
    assertEquals(400, response.getStatus());
  }

  @Test
  public void verifyInvalidInputExceptionMapperNullInputs() {
    assertThrows(NullPointerException.class, () -> {
      new InvalidInputExceptionMapper().toResponse(null);
    });
  }

  @Test
  public void verifyInvalidInputExceptionMapperResponse() {
    final Response response = new InvalidInputExceptionMapper().toResponse(
        new InvalidInput());
    assertEquals(400, response.getStatus());
  }


  @Test
  public void verifyInvalidJsonApiResourceExceptionMapperNullInputs() {
    assertThrows(NullPointerException.class, () -> {
      new InvalidJsonApiResourceExceptionMapper().toResponse(null);
    });
  }

  @Test
  public void verifyInvalidJsonApiResourceExceptionMapperResponse() {
    final Response response = new InvalidJsonApiResourceExceptionMapper().toResponse(
        new InvalidJsonApiResourceException("Doh!"));
    assertEquals(400, response.getStatus());
  }

  @Test
  public void verifyDocumentSerializationExceptionMapperMapperNullInputs() {
    assertThrows(NullPointerException.class, () -> {
      new DocumentSerializationExceptionMapper().toResponse(null);
    });
  }

  @Test
  public void verifyDocumentSerializationExceptionMapperMapperResponse() {
    final Response response = new DocumentSerializationExceptionMapper().toResponse(
        new DocumentSerializationException(new Exception("Doh!")));
    assertEquals(500, response.getStatus());
  }
}
