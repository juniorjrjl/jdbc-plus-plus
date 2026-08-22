package br.com.jdbcpp.sample.toimpl.inputparam;

import br.com.jdbcpp.api.method.write.Command;
import br.com.jdbcpp.api.input.InputParam;
import br.com.jdbcpp.sample.domain.Currency;
import br.com.jdbcpp.sample.domain.ProductInputParam;

import java.sql.SQLException;

import static br.com.jdbcpp.api.method.write.CommandType.DELETE;
import static br.com.jdbcpp.api.method.write.CommandType.INSERT;
import static br.com.jdbcpp.api.method.write.CommandType.UPDATE;

//@DAO
public interface InputParamSample {

    @Command(value = "DELETE FROM user WHERE id = :id:;", commandType = DELETE)
    void deleteById(@InputParam(statementField = "id") final long userId) throws SQLException;

    @Command(value = """
            INSERT INTO product (
                measurement_unit,
                product_nature,
                currency
            ) VALUES (
                :measurementUnit:,
                :productNature:,
                :currency:
            );""", commandType = INSERT)
    void insertProduct(final ProductInputParam productInputParam) throws SQLException;

    @Command(value = "UPDATE product SET currency = :currency:;", commandType = UPDATE)
    void updateAllProducts(@InputParam final Currency currency) throws SQLException;

}
