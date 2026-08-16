package br.com.jdbcpp.util.extension.postgres;

import br.com.jdbcpp.util.extension.DBTestContainerExtension;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.time.Duration;

public class PostgreSQLTestContainerExtension extends DBTestContainerExtension<PGContainer, PostgreSQLContainer>
        implements BeforeAllCallback, BeforeEachCallback {

    public PostgreSQLTestContainerExtension(){
        super(
                new PostgreSQLContainer("postgres:18.6-alpine")
                        .withCommand("postgres", "-c", "max_connections=500")
                        .withStartupTimeout(Duration.ofSeconds(60))
                        .waitingFor(Wait.forListeningPort()),
                PGContainer.class,
                PostgreSQLContainer.class);
    }

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        container.start();
        injectContainerInstance(context.getRequiredTestClass());
        runFlyway("classpath:db/migration/postgresql");
    }

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        try (final var connection = DriverManager.getConnection(
                container.getJdbcUrl(),
                container.getUsername(),
                container.getPassword());
             final var statement = connection.createStatement()) {

            for(final var table : TABLES){
                statement.execute(String.format("TRUNCATE TABLE %s RESTART IDENTITY CASCADE;", table));
            }
        }
    }

    protected DataSource getDataSource(){
        final var dataSource = new PGSimpleDataSource();
        dataSource.setUrl(container.getJdbcUrl());
        dataSource.setUser(container.getUsername());
        dataSource.setPassword(container.getPassword());
        return dataSource;
    }

}
