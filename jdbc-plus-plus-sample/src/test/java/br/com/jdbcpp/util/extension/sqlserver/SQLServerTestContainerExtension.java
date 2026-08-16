package br.com.jdbcpp.util.extension.sqlserver;

import br.com.jdbcpp.util.extension.DBTestContainerExtension;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.mssqlserver.MSSQLServerContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.time.Duration;

public class SQLServerTestContainerExtension extends DBTestContainerExtension<SQLServerContainer, MSSQLServerContainer>
        implements BeforeAllCallback, BeforeEachCallback {

    public SQLServerTestContainerExtension() {
        super(
                new MSSQLServerContainer("mcr.microsoft.com/mssql/server:2022-latest")
                        .acceptLicense()
                        .withStartupTimeout(Duration.ofSeconds(90))
                        .waitingFor(Wait.forListeningPort()),
                SQLServerContainer.class,
                MSSQLServerContainer.class);
    }

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        container.start();
        injectContainerInstance(context.getRequiredTestClass());
        runFlyway("classpath:db/migration/sqlserver");
    }

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        try (final var connection = DriverManager.getConnection(
                container.getJdbcUrl(),
                container.getUsername(),
                container.getPassword());
             final var statement = connection.createStatement()) {

            statement.execute("EXEC sp_MSforeachtable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL';");

            for (final var table : TABLES) {
                statement.execute(String.format("DELETE FROM %s;", table));
                statement.execute(String.format("IF OBJECTPROPERTY(OBJECT_ID('%s'), 'TableHasIdentity') = 1 DBCC CHECKIDENT ('%s', RESEED, 0);", table, table));
            }

            statement.execute("EXEC sp_MSforeachtable 'ALTER TABLE ? WITH CHECK CHECK CONSTRAINT ALL';");
        }
    }

    @Override
    protected DataSource getDataSource() {
        final var dataSource = new SQLServerDataSource();
        dataSource.setURL(container.getJdbcUrl());
        dataSource.setUser(container.getUsername());
        dataSource.setPassword(container.getPassword());
        dataSource.setTrustServerCertificate(true);
        return dataSource;
    }
}