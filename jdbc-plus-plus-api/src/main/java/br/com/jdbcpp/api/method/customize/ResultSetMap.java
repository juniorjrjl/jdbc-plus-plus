package br.com.jdbcpp.api.method.customize;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Used to call a custom method to map {@link java.sql.ResultSet} to operation return.
 * <p>
 * The method must return a collection of objects.
 * </p>
 * */
@Retention(SOURCE)
@Target(METHOD)
public @interface ResultSetMap {

    /**
     * method in DAO called, the method must receive a {@link java.sql.ResultSet} as parameter and return
     * the same type from method customized
     * */
    String value();

}
