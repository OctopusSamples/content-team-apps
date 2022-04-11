package com.octopus.json;

import com.octopus.exceptions.JsonSerializationException;
import com.octopus.json.impl.JacksonJsonSerializerImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JacksonJsonSerializerImplTest {

  @Test
  public void testSerialization() {
    Assertions.assertDoesNotThrow(
        () -> new JacksonJsonSerializerImpl().toJson(new JsonTestClass()));
    Assertions.assertThrows(NullPointerException.class,
        () -> new JacksonJsonSerializerImpl().toJson(null));
    Assertions.assertThrows(JsonSerializationException.class,
        () -> new JacksonJsonSerializerImpl().toJson(new WillNotSerialize()));
    Assertions.assertTrue(
        StringUtils.isNotBlank(new JacksonJsonSerializerImpl().toJson(new JsonTestClass())));
  }

  private static final class JsonTestClass {

    private int count = 0;
    private String value = "hi";

    public int getCount() {
      return count;
    }

    public void setCount(int count) {
      this.count = count;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }

  private static class WillNotSerialize {

    public WillNotSerialize getSelf() {
      return this;
    }
  }
}
