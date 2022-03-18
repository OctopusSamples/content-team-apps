package com.octopus.http;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.octopus.http.impl.CookieDateUtilsImpl;
import java.time.temporal.ChronoUnit;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

public class CookieDateUtilsImplTest {
  private static final CookieDateUtilsImpl COOKIE_DATE_UTILS = new CookieDateUtilsImpl();

  @Test
  public void TestCookieDateUtils() {
    final String date = COOKIE_DATE_UTILS.getRelativeExpiryDate(2, ChronoUnit.HOURS);
    assertTrue(!StringUtils.isAllBlank(date));
  }
}
