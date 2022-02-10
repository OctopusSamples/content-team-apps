package com.octopus.audits;

import java.util.function.Function;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class LambdaMatcher<T> extends BaseMatcher<T> {
  private final Function<T, Boolean> matcher;
  private final String description;

  public LambdaMatcher(Function<T, Boolean> matcher, String description) {
    this.matcher = matcher;
    this.description = description;
  }

  @Override
  public boolean matches(Object argument) {
    return matcher.apply((T) argument);
  }

  @Override
  public void describeTo(Description description) {
    description.appendText(this.description);
  }
}
