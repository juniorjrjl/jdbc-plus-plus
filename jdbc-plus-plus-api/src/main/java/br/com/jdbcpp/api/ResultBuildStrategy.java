package br.com.jdbcpp.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;
//TODO: add javadoc to mark result class to determinate value set strategy
@Retention(SOURCE)
@Target(METHOD)
public @interface ResultBuildStrategy {

    ResultBuildStrategyType value();

    Class<? extends Collection<?>> collectionImplementationResult();

}
