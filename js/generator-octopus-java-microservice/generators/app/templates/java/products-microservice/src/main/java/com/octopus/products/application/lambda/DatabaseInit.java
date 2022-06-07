package com.octopus.products.application.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.octopus.products.infrastructure.utilities.LiquidbaseUpdater;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import io.quarkus.logging.Log;
import io.vavr.control.Try;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import liquibase.exception.LiquibaseException;

/**
 * The Lambda entry point used to execute database migrations.
 */
@Named("DatabaseInit")
public class DatabaseInit implements RequestHandler<Map<String, Object>, APIGatewayProxyResponseEvent> {

  /**
   * A retry policy used when calling upstream services.
   */
  private static final RetryPolicy<APIGatewayProxyResponseEvent> RETRY_POLICY = RetryPolicy
      .<APIGatewayProxyResponseEvent>builder()
      .handle(Exception.class)
      .withDelay(Duration.ofSeconds(30))
      .withMaxRetries(10)
      .build();

  @Inject
  LiquidbaseUpdater liquidbaseUpdater;

  @Override
  public APIGatewayProxyResponseEvent handleRequest(
      final Map<String, Object> stringObjectMap, final Context context) {

    /*
      This migration can be run directly after the database infrastructure
      is created. CloudFormation can return before the database can be
      accessed though, so this logic repeats forever until the migration
      succeeds or the Lambda times out.
     */

    return Failsafe.with(RETRY_POLICY)
        .onFailure(e -> Log.error("Failed to migrate database", e.getException()))
        .get(() -> {
          liquidbaseUpdater.update();
          return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody("ok");
        });
  }
}
