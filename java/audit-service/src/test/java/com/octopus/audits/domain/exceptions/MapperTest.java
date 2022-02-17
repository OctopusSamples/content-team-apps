package com.octopus.audits.domain.exceptions;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.octopus.audits.domain.framework.providers.DocumentSerializationExceptionMapper;
import com.octopus.audits.domain.framework.providers.EntityNotFoundMapper;
import com.octopus.audits.domain.framework.providers.InvalidAcceptHeadersMapper;
import com.octopus.audits.domain.framework.providers.InvalidInputExceptionMapper;
import com.octopus.audits.domain.framework.providers.InvalidJsonApiResourceExceptionMapper;
import com.octopus.audits.domain.framework.providers.RsqlParserExceptionMapper;
import org.junit.jupiter.api.Test;

public class MapperTest {
  @Test
  public void verifyInvalidAcceptHeadersMapperNullInputs() {
    assertThrows(NullPointerException.class, () -> {
      new InvalidAcceptHeadersMapper().toResponse(null);
    });
  }

  @Test
  public void verifyEntityNotFoundMapperNullInputs() {
    assertThrows(NullPointerException.class, () -> {
      new EntityNotFoundMapper().toResponse(null);
    });
  }

  @Test
  public void verifyRsqlParserExceptionMapperNullInputs() {
    assertThrows(NullPointerException.class, () -> {
      new RsqlParserExceptionMapper().toResponse(null);
    });
  }

  @Test
  public void verifyInvalidInputExceptionMapperNullInputs() {
    assertThrows(NullPointerException.class, () -> {
      new InvalidInputExceptionMapper().toResponse(null);
    });
  }

  @Test
  public void verifyInvalidJsonApiResourceExceptionMapperNullInputs() {
    assertThrows(NullPointerException.class, () -> {
      new InvalidJsonApiResourceExceptionMapper().toResponse(null);
    });
  }

  @Test
  public void verifyDocumentSerializationExceptionMapperMapperNullInputs() {
    assertThrows(NullPointerException.class, () -> {
      new DocumentSerializationExceptionMapper().toResponse(null);
    });
  }
}
