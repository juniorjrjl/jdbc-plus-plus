package br.com.jdbcpp.dao;

import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.api.method.read.Query;
import br.com.jdbcpp.api.method.read.ResultBuildStrategy;
import br.com.jdbcpp.dto.user.select.UserClassConstructor;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static br.com.jdbcpp.api.method.read.ResultBuildStrategyType.CONSTRUCTOR;

@DAO
public interface SelectUserDAO {

    @Query(value = """
            SELECT id,
                   user_identifier,
                   first_name,
                   last_name,
                   email,
                   birth_date,
                   created_at,
                   updated_at
              FROM users
             WHERE id = :id:
            """)
    @ResultBuildStrategy(CONSTRUCTOR)
    Optional<UserClassConstructor> selectOptionalById(final UUID id) throws SQLException;

    @Query(value = """
            SELECT id,
                   user_identifier,
                   first_name,
                   last_name,
                   email,
                   birth_date,
                   created_at,
                   updated_at
              FROM users
            """)
    @ResultBuildStrategy(CONSTRUCTOR)
    List<UserClassConstructor> findAll() throws SQLException;

}
