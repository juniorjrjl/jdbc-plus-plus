package br.com.jdbcpp.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;
//TODO: add javadoc to mark props in result class belongs to read methods
@Target({PARAMETER, METHOD})
@Retention(SOURCE)
public @interface PropStrategy {

    String value();

    int resultSetIndex() default -1;

    String setter();

    boolean ignore() default false;

}
