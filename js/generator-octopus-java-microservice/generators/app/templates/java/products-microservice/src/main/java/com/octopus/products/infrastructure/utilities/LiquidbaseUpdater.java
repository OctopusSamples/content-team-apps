package com.octopus.products.infrastructure.utilities;

import java.sql.Connection;
import java.sql.SQLException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

/** A class that exposes a method to manually apply Liquidbase migrations. */
@ApplicationScoped
public class LiquidbaseUpdater {
  @Inject DataSource defaultDataSource;

  /**
   * Apply any pending Liquidbase migrations.
   *
   * @throws SQLException Thrown by liquidbase.
   * @throws LiquibaseException Thrown by liquidbase.
   */
  public void update() throws SQLException, LiquibaseException {
    try (Connection connection = defaultDataSource.getConnection()) {
      final Database database =
          DatabaseFactory.getInstance()
              .findCorrectDatabaseImplementation(new JdbcConnection(connection));
      final Liquibase liquibase =
          new Liquibase("db/changeLog.xml", new ClassLoaderResourceAccessor(), database);
      liquibase.update(new Contexts(), new LabelExpression());
    }
  }
}
