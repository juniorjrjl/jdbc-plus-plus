package br.com.jdbcpp.insert;

import br.com.jdbcpp.util.DatabaseCapability;
import br.com.jdbcpp.util.extension.sqlserver.SQLServerContainer;
import br.com.jdbcpp.util.extension.sqlserver.SQLServerTestContainerExtension;
import br.com.jdbcpp.util.tag.SQLServerTest;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.mssqlserver.MSSQLServerContainer;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

@ExtendWith(SQLServerTestContainerExtension.class)
@SQLServerTest
public class SQLServerInsertTest extends InsertTest{

    @SQLServerContainer
    private static MSSQLServerContainer dbContainer;

    @Override
    protected List<DatabaseCapability> capabilities() {
        return List.of(DatabaseCapability.GENERATED_KEYS_BY_INDEX);
    }

    public DataSource getDataSource() throws SQLException {
        final var dataSource = new SQLServerDataSource();
        dataSource.setURL(dbContainer.getJdbcUrl());
        dataSource.setUser(dbContainer.getUsername());
        dataSource.setPassword(dbContainer.getPassword());
        return dataSource;
    }

}
