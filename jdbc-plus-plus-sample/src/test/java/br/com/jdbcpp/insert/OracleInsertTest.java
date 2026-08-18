package br.com.jdbcpp.insert;

import br.com.jdbcpp.util.DatabaseCapability;
import br.com.jdbcpp.util.extension.oracle.OracleDBContainer;
import br.com.jdbcpp.util.extension.oracle.OracleTestContainerExtension;
import br.com.jdbcpp.util.tag.OracleTest;
import oracle.jdbc.pool.OracleDataSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.oracle.OracleContainer;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

@ExtendWith(OracleTestContainerExtension.class)
@OracleTest
public class OracleInsertTest extends InsertTest{

    @OracleDBContainer
    private static OracleContainer dbContainer;

    @Override
    protected List<DatabaseCapability> capabilities() {
        return List.of(DatabaseCapability.GENERATED_KEYS_BY_INDEX);
    }

    public DataSource getDataSource() throws SQLException {
        final var dataSource = new OracleDataSource();
        dataSource.setURL(dbContainer.getJdbcUrl());
        dataSource.setUser(dbContainer.getUsername());
        dataSource.setPassword(dbContainer.getPassword());
        return dataSource;
    }

}
