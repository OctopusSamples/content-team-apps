package com.octopus.http;


import io.vavr.control.Try;
import java.util.List;
import org.apache.http.Header;

/**
 * Defines a read only HTTP client used to access files from git repos.
 */
public interface HttpClient {

  Try<String> get(String url);

  Try<String> get(String url, String username, String password);

  Try<String> get(String url, List<Header> headers);

  boolean head(String url);

  boolean head(String url, String username, String password);

  boolean head(String url, List<Header> headers);

}
