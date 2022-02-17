package com.octopus.audits.domain.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.audits.domain.utilities.RegExUtils;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

public class RegExUtilsTest {
  @Test
  public void testNulls() {
    assertThrows(NullPointerException.class, () -> {
      RegExUtils.getGroup(null, "", "");
    });

    assertThrows(NullPointerException.class, () -> {
      RegExUtils.getGroup(Pattern.compile("."), "", null);
    });
  }

  @Test
  public void testGroupReturned() {
    assertEquals("hi", RegExUtils.getGroup(Pattern.compile("(?<group>.+)"), "hi", "group").get());
    assertTrue(RegExUtils.getGroup(Pattern.compile("(?<group>.+)"), null, "group").isEmpty());
    assertTrue(RegExUtils.getGroup(Pattern.compile("(?<group>.+)"), "hi", "missing").isEmpty());
    assertTrue(RegExUtils.getGroup(Pattern.compile("(?<group>hi)"), "blah", "group").isEmpty());
  }
}
