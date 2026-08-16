package br.com.jdbcpp.insert;

import br.com.jdbcpp.util.extension.mysql.MySQLServerContainer;
import br.com.jdbcpp.util.extension.mysql.MySQLTestContainerExtension;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.mysql.MySQLContainer;

import javax.sql.DataSource;

@ExtendWith(MySQLTestContainerExtension.class)
public class MySQLInsertTest extends InsertTest{

    @MySQLServerContainer
    private static MySQLContainer dbContainer;

    public DataSource getDataSource(){
        final var dataSource = new MysqlDataSource();
        dataSource.setUrl(dbContainer.getJdbcUrl());
        dataSource.setUser(dbContainer.getUsername());
        dataSource.setPassword(dbContainer.getPassword());
        return dataSource;
    }

}
