package com.octopus.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.utilties.RegExUtils;
import com.octopus.utilties.impl.RegExUtilsImpl;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

public class RegExUtilsTest {
  private static final RegExUtils REG_EX_UTILS = new RegExUtilsImpl();

  @Test
  public void testNulls() {
    assertThrows(NullPointerException.class, () -> {
      REG_EX_UTILS.getGroup(null, "", "");
    });

    assertThrows(NullPointerException.class, () -> {
      REG_EX_UTILS.getGroup(Pattern.compile("."), "", null);
    });
  }

  @Test
  public void testGroupReturned() {
    assertEquals("hi", REG_EX_UTILS.getGroup(Pattern.compile("(?<group>.+)"), "hi", "group").get());
    assertTrue(REG_EX_UTILS.getGroup(Pattern.compile("(?<group>.+)"), null, "group").isEmpty());
    assertTrue(REG_EX_UTILS.getGroup(Pattern.compile("(?<group>.+)"), "hi", "missing").isEmpty());
    assertTrue(REG_EX_UTILS.getGroup(Pattern.compile("(?<group>hi)"), "blah", "group").isEmpty());
  }
}
