package com.octopus.products.application.http;

import java.util.function.Function;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * A class used for custom hamcrest matches used in fluent testing.
 *
 * @param <T> The class that is passed into the matcher function.
 */
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
