package br.com.jdbcpp.util.extension.oracle;

import br.com.jdbcpp.util.extension.DBTestContainerExtension;
import oracle.jdbc.pool.OracleDataSource;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.oracle.OracleContainer;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.time.Duration;

public class OracleTestContainerExtension
        extends DBTestContainerExtension<OracleDBContainer, OracleContainer>
        implements BeforeAllCallback, BeforeEachCallback {

    public OracleTestContainerExtension() {
        super(
                new OracleContainer("gvenzl/oracle-free:23-slim")
                        .withStartupTimeout(Duration.ofSeconds(180)),
                OracleDBContainer.class,
                OracleContainer.class);
    }

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        container.start();
        injectContainerInstance(context.getRequiredTestClass());
        runFlyway("classpath:db/migration/oracle");
    }

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        try (final var connection = DriverManager.getConnection(
                container.getJdbcUrl(),
                container.getUsername(),
                container.getPassword());
             final var statement = connection.createStatement()) {

            for (final var table : TABLES) {
                statement.execute(
                        String.format("TRUNCATE TABLE %s", table)
                );
            }
        }
    }

    @Override
    protected DataSource getDataSource() {
        try {
            final var dataSource = new OracleDataSource();

            dataSource.setURL(container.getJdbcUrl());
            dataSource.setUser(container.getUsername());
            dataSource.setPassword(container.getPassword());

            return dataSource;
        } catch (Exception e) {
            throw new IllegalStateException("Could not create Oracle DataSource", e);
        }
    }
}