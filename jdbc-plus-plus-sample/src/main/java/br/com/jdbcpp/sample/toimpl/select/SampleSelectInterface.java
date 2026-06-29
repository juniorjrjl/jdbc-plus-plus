package br.com.jdbcpp.sample.toimpl.select;

import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.api.Query;
import br.com.jdbcpp.api.ResultBuildStrategy;
import br.com.jdbcpp.api.ResultBuildStrategyType;
import br.com.jdbcpp.sample.domain.Employee;

import java.sql.SQLException;
import java.util.Optional;

@DAO
public interface SampleSelectInterface {

    @Query("""
           SELECT id,
                  name,
                  email,
                  birth_date
             FROM user
            WHERE id = :id:
           """)
    @ResultBuildStrategy(value = ResultBuildStrategyType.CONSTRUCTOR)
    Employee findById(final Long id) throws SQLException;

    @Query("""
           SELECT id,
                  name,
                  email,
                  birth_date
             FROM user
            WHERE id = :id:
           """)
    @ResultBuildStrategy(value = ResultBuildStrategyType.CONSTRUCTOR)
    Optional<Employee> findOptionalById(final Long id) throws SQLException;

}
