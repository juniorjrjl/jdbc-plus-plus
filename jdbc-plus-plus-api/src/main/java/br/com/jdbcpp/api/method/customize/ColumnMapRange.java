package br.com.jdbcpp.api.method.customize;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Used to call a custom method to map columns to a range of objects.
 * <p>
 * The method must return a collection of objects.
 * </p>
 * */
@Repeatable(ColumnMapRanges.class)
@Retention(SOURCE)
@Target(METHOD)
public @interface ColumnMapRange {

    /**
     * method in DAO called, the method must receive a {@link java.sql.ResultSet} as parameter and index to get param and return
     * the type expected in position
     * */
    String value();

    /**
     * inclusive input param position start
     * */
    int start();

    /**
     * inclusive input param position end
     * */
    int end();


}
