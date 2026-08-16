package br.com.jdbcpp.dao;

import br.com.jdbcpp.api.Command;
import br.com.jdbcpp.api.DAO;

import java.sql.SQLException;

@DAO
public interface CategoryDAO {

    @Command(value = """
            INSERT INTO categories (name, created_at, updated_at)
            VALUES ('food', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
            """)
    void insertFixData() throws SQLException;

}
