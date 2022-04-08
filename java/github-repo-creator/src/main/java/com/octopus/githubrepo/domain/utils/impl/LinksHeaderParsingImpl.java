package com.octopus.githubrepo.domain.utils.impl;

import com.octopus.githubrepo.domain.utils.LinksHeaderParsing;
import java.util.Arrays;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;

/**
 * Implementation of LinksHeaderParsing.
 */
@ApplicationScoped
public class LinksHeaderParsingImpl implements LinksHeaderParsing {

  @Override
  public Optional<String> getLastPage(final String link) {
    if (StringUtils.isBlank(link)) {
      return Optional.empty();
    }

    // split on commas
    return Arrays.stream(link.split(","))
        .map(String::trim)
        // split on semi colons
        .map(l -> l.split(";"))
        // We expect to find a link and a relationship
        .filter(a -> a.length == 2)
        // The relationship we are looking for is called "last"
        .filter(a -> a[1].trim().endsWith("\"last\""))
        // get the link
        .map(a -> a[0])
        // remove the angle brackets
        .map(l -> l.replace("<", "").replace(">", ""))
        // split on query params
        .flatMap(l -> Arrays.stream(l.split("&|\\?")))
        // find the param that starts with page
        .filter(q -> q.startsWith("page="))
        // split that param on the equals
        .map(q -> q.split("="))
        // We expect to find two components
        .filter(a -> a.length == 2)
        // Get the last component
        .map(a -> a[1])
        // That is the last page
        .findFirst();
  }
}
