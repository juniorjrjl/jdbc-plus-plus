package br.com.jdbcpp.util.extension.mysql;

import br.com.jdbcpp.util.extension.DBTestContainerExtension;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.mysql.MySQLContainer;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.time.Duration;

public class MySQLTestContainerExtension  extends DBTestContainerExtension<MySQLServerContainer, MySQLContainer>
        implements BeforeAllCallback, BeforeEachCallback {

    public MySQLTestContainerExtension() {
        super(
                new MySQLContainer("mysql:8.0")
                        .withCommand("--max_connections=500")
                        .withStartupTimeout(Duration.ofSeconds(60))
                        .waitingFor(Wait.forListeningPort()),
                MySQLServerContainer.class,
                MySQLContainer.class);
    }

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        container.start();
        injectContainerInstance(context.getRequiredTestClass());
        runFlyway("classpath:db/migration/mysql");
    }

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        try (final var connection = DriverManager.getConnection(
                container.getJdbcUrl(),
                container.getUsername(),
                container.getPassword());
             final var statement = connection.createStatement()) {

            statement.execute("SET FOREIGN_KEY_CHECKS = 0;");

            for (final var table : TABLES) {
                statement.execute(String.format("TRUNCATE TABLE %s;", table));
            }

            statement.execute("SET FOREIGN_KEY_CHECKS = 1;");
        }
    }

    protected DataSource getDataSource(){
        final var dataSource = new MysqlDataSource();
        dataSource.setUrl(container.getJdbcUrl());
        dataSource.setUser(container.getUsername());
        dataSource.setPassword(container.getPassword());
        return dataSource;
    }

}
