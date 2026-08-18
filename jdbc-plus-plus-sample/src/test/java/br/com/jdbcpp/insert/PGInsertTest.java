package br.com.jdbcpp.insert;

import br.com.jdbcpp.util.DatabaseCapability;
import br.com.jdbcpp.util.extension.postgres.PGContainer;
import br.com.jdbcpp.util.extension.postgres.PostgreSQLTestContainerExtension;
import br.com.jdbcpp.util.tag.PostgreSQLTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;
import java.util.List;

import static br.com.jdbcpp.util.DatabaseCapability.GENERATED_KEYS_BY_INDEX;

@ExtendWith(PostgreSQLTestContainerExtension.class)
@PostgreSQLTest
public class PGInsertTest extends InsertTest{

    @PGContainer
    private static PostgreSQLContainer dbContainer;

    @Override
    protected List<DatabaseCapability> capabilities() {
        return List.of(DatabaseCapability.PG_TEST_CLASS_GETTER_INSERT);
    }

    public DataSource getDataSource(){
        final var dataSource = new PGSimpleDataSource();
        dataSource.setUrl(dbContainer.getJdbcUrl());
        dataSource.setUser(dbContainer.getUsername());
        dataSource.setPassword(dbContainer.getPassword());
        return dataSource;
    }

}
