package br.com.jdbcpp.insert;

import br.com.jdbcpp.util.DatabaseCapability;
import br.com.jdbcpp.util.extension.mysql.MySQLServerContainer;
import br.com.jdbcpp.util.extension.mysql.MySQLTestContainerExtension;
import br.com.jdbcpp.util.tag.MySQLTest;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.mysql.MySQLContainer;

import javax.sql.DataSource;
import java.util.List;

@ExtendWith(MySQLTestContainerExtension.class)
@MySQLTest
public class MySQLInsertTest extends InsertTest{

    @MySQLServerContainer
    private static MySQLContainer dbContainer;

    @Override
    protected List<DatabaseCapability> capabilities() {
        return List.of(DatabaseCapability.GENERATED_KEYS_BY_INDEX);
    }

    public DataSource getDataSource(){
        final var dataSource = new MysqlDataSource();
        dataSource.setUrl(dbContainer.getJdbcUrl());
        dataSource.setUser(dbContainer.getUsername());
        dataSource.setPassword(dbContainer.getPassword());
        return dataSource;
    }

}
