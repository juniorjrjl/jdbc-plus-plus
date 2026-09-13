package br.com.jdbcpp.api.method.customize;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Used to call a custom method to map statement inputs in some range
 * */
@Repeatable(InputMapRanges.class)
@Retention(SOURCE)
@Target(METHOD)
public @interface InputMapRange {

    /**
     * method in DAO called, the method must receive in first {@link java.sql.PreparedStatement}
     * and same params used in method customized in same order
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
