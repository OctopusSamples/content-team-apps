package com.octopus.products.application;

/**
 * The paths used for tests. Update these to reflect the paths exposed by the application.
 *
 * <p>The health check endpoints are inspired by the Kubernetes livez API endpoint (
 * https://kubernetes.io/docs/reference/using-api/health-checks/#individual-health-checks) where
 * each individual service can be queried for its health with a call to
 * a URL like https://localhost:6443/livez/etcd.
 *
 * <p>In this microservice architecture, we assume each path and HTTP method could be served by a
 * unique microsrevice. This means each service is identified by the JSON API path (i.e. products
 * or products/1) and the HTTP method (i.e. GET) to create an identifier /products/POST or
 * /products/1/GET.
 *
 * <p>This means a GET request to /health/products/POST will return the health of the microservice
 * responsible for creating new products.
 */
public final class Paths {
  public static final String API_ENDPOINT = "/api/products";
  public static final String HEALTH_ENDPOINT = "/health/products";
}
