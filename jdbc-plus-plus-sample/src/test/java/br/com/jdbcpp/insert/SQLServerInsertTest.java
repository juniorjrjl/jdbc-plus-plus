package br.com.jdbcpp.insert;

import br.com.jdbcpp.util.extension.sqlserver.SQLServerContainer;
import br.com.jdbcpp.util.extension.sqlserver.SQLServerTestContainerExtension;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.mssqlserver.MSSQLServerContainer;

import javax.sql.DataSource;
import java.sql.SQLException;

@ExtendWith(SQLServerTestContainerExtension.class)
public class SQLServerInsertTest extends InsertTest{

    @SQLServerContainer
    private static MSSQLServerContainer dbContainer;

    public DataSource getDataSource() throws SQLException {
        final var dataSource = new SQLServerDataSource();
        dataSource.setURL(dbContainer.getJdbcUrl());
        dataSource.setUser(dbContainer.getUsername());
        dataSource.setPassword(dbContainer.getPassword());
        return dataSource;
    }

}
