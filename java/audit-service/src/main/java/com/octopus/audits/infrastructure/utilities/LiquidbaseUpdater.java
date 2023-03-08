package com.octopus.audits.infrastructure.utilities;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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

  public void update() throws SQLException, LiquibaseException {
    update(false);
  }

  /**
   * Apply any pending Liquidbase migrations.
   *
   * @throws SQLException Thrown by liquidbase.
   * @throws LiquibaseException Thrown by liquidbase.
   */
  public void update(final boolean clearLock) throws SQLException, LiquibaseException {
    try (final Connection connection = defaultDataSource.getConnection()) {
      if (clearLock) {
        clearLock(connection);
      }

      final Database database =
          DatabaseFactory.getInstance()
              .findCorrectDatabaseImplementation(new JdbcConnection(connection));
      final Liquibase liquibase =
          new Liquibase("db/changeLog.xml", new ClassLoaderResourceAccessor(), database);
      liquibase.update(new Contexts(), new LabelExpression());
    }
  }

  private void clearLock(final Connection connection) throws SQLException {
    String query = "UPDATE DATABASECHANGELOGLOCK SET locked=false, lockgranted=null, lockedby=null WHERE id=1;";
    try (final Statement stmt = connection.createStatement()) {
      stmt.execute(query);
    }
  }
}
