package br.com.jdbcpp.dao.abstracts;

import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.api.method.read.Query;
import br.com.jdbcpp.dto.category.select.CategoryInsertedClassDTO;
import br.com.jdbcpp.dto.category.select.CategoryInsertedClassSetterIndexDTO;
import br.com.jdbcpp.dto.category.select.CategoryInsertedCustomSetterDTO;
import br.com.jdbcpp.dto.category.select.CategoryInsertedRecordDTO;
import br.com.jdbcpp.exception.CustomSQLException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@DAO
public abstract class SelectCategoryDAO {

    protected final DataSource dataSource;

    public SelectCategoryDAO(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Query(value = """
            SELECT name,
                   created_at,
                   updated_at
              FROM categories
            """)
    public abstract List<CategoryInsertedRecordDTO> selectAll() throws SQLException;

    @Query(value = """
            SELECT name,
                   created_at,
                   updated_at
              FROM categories
            """, packException = CustomSQLException.class)
    public abstract List<CategoryInsertedClassSetterIndexDTO> selectAllClassIndex();

    @Query(value = """
            SELECT name,
                   priority,
                   created_at,
                   updated_at
              FROM categories
            """)
    public abstract Set<CategoryInsertedCustomSetterDTO> selectAllWithPriority() throws SQLException;

    @Query(value = """
            SELECT name,
                   created_at,
                   updated_at
              FROM categories
             WHERE id = :id:
            """)
    public abstract CategoryInsertedRecordDTO findById(final long id) throws SQLException;

    @Query(value = """
            SELECT id,
                   name,
                   created_at,
                   updated_at
              FROM categories
             WHERE id = :id:
            """)
    public abstract Optional<CategoryInsertedClassDTO> findOptionalById(final long id) throws SQLException;

}
