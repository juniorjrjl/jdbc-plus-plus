package br.com.jdbcpp.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;
//TODO: add javadoc to mark read methods
@Retention(SOURCE)
@Target(METHOD)
public @interface Query {

    String value() default "";

}
