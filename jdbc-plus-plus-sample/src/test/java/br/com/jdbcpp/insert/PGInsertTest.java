package br.com.jdbcpp.insert;

import br.com.jdbcpp.util.extension.postgres.PGContainer;
import br.com.jdbcpp.util.extension.postgres.PostgreSQLTestContainerExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;

@ExtendWith(PostgreSQLTestContainerExtension.class)
public class PGInsertTest extends InsertTest{

    @PGContainer
    private static PostgreSQLContainer dbContainer;

    public DataSource getDataSource(){
        final var dataSource = new PGSimpleDataSource();
        dataSource.setUrl(dbContainer.getJdbcUrl());
        dataSource.setUser(dbContainer.getUsername());
        dataSource.setPassword(dbContainer.getPassword());
        return dataSource;
    }

}
