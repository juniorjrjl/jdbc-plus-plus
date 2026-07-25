package br.com.jdbcpp.sample.toimpl.commandquery.select;

import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.api.Query;
import br.com.jdbcpp.api.ResultBuildStrategy;
import br.com.jdbcpp.api.ResultBuildStrategyType;
import br.com.jdbcpp.sample.domain.Employee;
import br.com.jdbcpp.sample.domain.ProductClass;
import br.com.jdbcpp.sample.exception.CustomException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    Employee findById(final Long id) throws SQLException ;

    @Query("""
           SELECT id,
                  name,
                  price,
                  amount,
                  created_at
             FROM product
            WHERE id = :id:
           """)
    @ResultBuildStrategy(value = ResultBuildStrategyType.CONSTRUCTOR)
    ProductClass findByIdClass(final Long id) throws SQLException ;

    @Query("""
           SELECT id,
                  name,
                  email,
                  birth_date
             FROM user
            WHERE id = :id:;
           """)
    @ResultBuildStrategy(value = ResultBuildStrategyType.CONSTRUCTOR)
    Optional<Employee> findOptionalId(final Long id) throws SQLException ;

    @Query("""
           SELECT id,
                  name,
                  email,
                  birth_date
             FROM user;
           """)
    @ResultBuildStrategy(value = ResultBuildStrategyType.CONSTRUCTOR)
    List<Employee> findAll() throws SQLException ;

    @Query("""
           SELECT id,
                  name,
                  email,
                  birth_date
             FROM user;
           """)
    @ResultBuildStrategy(value = ResultBuildStrategyType.CONSTRUCTOR)
    Set<Employee> findAllSet() throws SQLException ;

    @Query("""
            SELECT id
              FROM user;
            """)
    List<Long> findAllId() throws SQLException ;

    @Query(value = """
            SELECT name
              FROM user;
            """,
            packException = CustomException.class)
    Optional<String> findNameById() throws SQLException ;

}
