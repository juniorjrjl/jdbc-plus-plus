package br.com.jdbcpp.dao;

import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.api.Query;
import br.com.jdbcpp.dto.category.insert.CategoryDTO;
import br.com.jdbcpp.dto.category.select.CategoryInsertedClassDTO;
import br.com.jdbcpp.dto.category.select.CategoryInsertedClassSetterIndexDTO;
import br.com.jdbcpp.dto.category.select.CategoryInsertedRecordDTO;
import br.com.jdbcpp.exception.CustomSQLException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@DAO
public interface SelectCategoryDAO {

    @Query(value = """
            SELECT name,
                   created_at,
                   updated_at
              FROM categories
            """)
    List<CategoryInsertedRecordDTO> selectAll() throws SQLException;

    @Query(value = """
            SELECT name,
                   created_at,
                   updated_at
              FROM categories
            """, packException = CustomSQLException.class)
    List<CategoryInsertedClassSetterIndexDTO> selectAllClassIndex();

    @Query(value = """
            SELECT name,
                   created_at,
                   updated_at
              FROM categories
             WHERE id = :id:
            """)
    CategoryInsertedRecordDTO findById(final long id) throws SQLException;

    @Query(value = """
            SELECT id,
                   name,
                   created_at,
                   updated_at
              FROM categories
             WHERE id = :id:
            """)
    Optional<CategoryInsertedClassDTO> findOptionalById(final long id) throws SQLException;

}
