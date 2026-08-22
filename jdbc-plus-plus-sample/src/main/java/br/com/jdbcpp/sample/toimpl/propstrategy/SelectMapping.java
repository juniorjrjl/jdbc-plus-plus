package br.com.jdbcpp.sample.toimpl.propstrategy;

import br.com.jdbcpp.api.method.read.Query;
import br.com.jdbcpp.api.method.read.ResultBuildStrategy;
import br.com.jdbcpp.api.method.read.ResultBuildStrategyType;
import br.com.jdbcpp.sample.domain.ProductClass;
import br.com.jdbcpp.sample.domain.ProductCustomSetter;
import br.com.jdbcpp.sample.domain.ProductIndexedClass;

import java.sql.SQLException;

//@DAO
public interface SelectMapping {


    @Query("""
           SELECT id,
                  name,
                  price,
                  amount,
                  created_at
             FROM product
            WHERE id = :id:
           """)
    @ResultBuildStrategy(value = ResultBuildStrategyType.SETTER)
    ProductClass findByIdClass(final Long id) throws SQLException;

    @Query("""
           SELECT id,
                  name,
                  price,
                  amount,
                  created_at
             FROM product
            WHERE id = :id:
           """)
    @ResultBuildStrategy(value = ResultBuildStrategyType.SETTER)
    ProductIndexedClass findByIdIndexedClass(final Long id) throws SQLException;

    @Query("""
           SELECT id,
                  price,
                  created_at
             FROM product
            WHERE id = :id:
           """)
    @ResultBuildStrategy(value = ResultBuildStrategyType.SETTER)
    ProductCustomSetter findByIdCustomSetter(final Long id) throws SQLException;

}
