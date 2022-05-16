package com.octopus.products.application.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.octopus.products.infrastructure.utilities.LiquidbaseUpdater;
import java.sql.SQLException;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import liquibase.exception.LiquibaseException;

/**
 * The Lambda entry point used to execute database migrations.
 */
@Named("DatabaseInit")
public class DatabaseInit implements RequestHandler<Map<String, Object>, APIGatewayProxyResponseEvent> {

  @Inject LiquidbaseUpdater liquidbaseUpdater;

  @Override
  public APIGatewayProxyResponseEvent handleRequest(
      final Map<String, Object> stringObjectMap, final Context context) {

    /*
      This migration can be run directly after the database infrastructure
      is created. CloudFormation can return before the database can be
      accessed though, so this logic repeats forever until the migration
      succeeds or the Lambda times out.
     */
    try {
      liquidbaseUpdater.update();
      return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody("ok");
    } catch (final LiquibaseException | SQLException ex) {
      return handleRequest(stringObjectMap, context);
    }
  }
}
