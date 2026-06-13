package br.com.jdbcpp.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static br.com.jdbcpp.api.CommandType.INSERT;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;
//TODO: add javadoc to mark write methods
@Retention(SOURCE)
@Target(METHOD)
public @interface Command {

    String value() default "";

    CommandType commandType() default INSERT;

    boolean returnRowsAffected() default false;

}
