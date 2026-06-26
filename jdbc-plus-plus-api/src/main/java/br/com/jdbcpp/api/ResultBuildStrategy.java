package br.com.jdbcpp.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Configures how query results should be built.
 * <p>
 * Determines whether class properties are populated via constructor or setter methods,
 * or if the result is a simple value. Also specifies the collection implementation
 * to use when the method return type is a collection interface.
 * </p>
 */
@Retention(SOURCE)
@Target(METHOD)
public @interface ResultBuildStrategy {

    ResultBuildStrategyType value();

    /**
     * The collection implementation to use when the method return type is a collection interface.
     * <p>
     * Required when the method returns a collection interface (e.g., {@code List}, {@code Set})
     * to specify the concrete implementation class.
     * </p>
     *
     * @return the collection implementation class
     */
    Class<? extends Collection<?>> collectionImplementationResult();

}
