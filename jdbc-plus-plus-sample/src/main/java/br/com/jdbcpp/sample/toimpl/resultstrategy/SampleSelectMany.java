package br.com.jdbcpp.sample.toimpl.resultstrategy;

import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.api.Query;
import br.com.jdbcpp.api.ResultBuildStrategy;
import br.com.jdbcpp.api.ResultBuildStrategyType;
import br.com.jdbcpp.sample.domain.Employee;

import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@DAO
public interface SampleSelectMany {

    @Query("""
           SELECT id,
                  name,
                  email,
                  birth_date
             FROM user;
           """)
    @ResultBuildStrategy(value = ResultBuildStrategyType.CONSTRUCTOR, collectionImplementationResult = LinkedList.class)
    List<Employee> findAll() throws SQLException;

    @Query("""
           SELECT id,
                  name,
                  email,
                  birth_date
             FROM user;
           """)
    @ResultBuildStrategy(value = ResultBuildStrategyType.CONSTRUCTOR, collectionImplementationResult = LinkedHashSet.class)
    Set<Employee> findAllSet() throws SQLException;

}
