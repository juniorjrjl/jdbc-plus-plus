package br.com.jdbcpp.insert;

import br.com.jdbcpp.util.extension.oracle.OracleDBContainer;
import br.com.jdbcpp.util.extension.oracle.OracleTestContainerExtension;
import oracle.jdbc.pool.OracleDataSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.oracle.OracleContainer;

import javax.sql.DataSource;
import java.sql.SQLException;

@ExtendWith(OracleTestContainerExtension.class)
public class OracleInsertTest extends InsertTest{

    @OracleDBContainer
    private static OracleContainer dbContainer;

    public DataSource getDataSource() throws SQLException {
        final var dataSource = new OracleDataSource();
        dataSource.setURL(dbContainer.getJdbcUrl());
        dataSource.setUser(dbContainer.getUsername());
        dataSource.setPassword(dbContainer.getPassword());
        return dataSource;
    }

}
