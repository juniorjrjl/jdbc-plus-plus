package br.com.jdbcpp.insert;

import br.com.jdbcpp.dao.CategoryDAO;
import br.com.jdbcpp.dao.CategoryDAOImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.sql.SQLException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

abstract class InsertTest {

    private CategoryDAO categoryDAO;

    public abstract DataSource getDataSource() throws SQLException;

    @BeforeEach
    public void setUp() throws SQLException  {
        categoryDAO = new CategoryDAOImpl(getDataSource());
    }

    @Test
    void insertFixData() {
        assertThatNoException().isThrownBy(() -> categoryDAO.insertFixData());
    }

}
