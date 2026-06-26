package br.com.jdbcpp.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marks methods that should implement read query logic.
 * <p>
 * Return type rules:
 * </p>
 * <ul>
 * <li>A single class instance</li>
 * <li>A collection of class instances (e.g., {@code List}, {@code Set})</li>
 * <li>An {@link java.util.Optional} of a class instance</li>
 * </ul>
 */
@Retention(SOURCE)
@Target(METHOD)
public @interface Query {

    /**
     * The SQL select statement to execute.
     * <p>
     * Must contain named parameters prefixed with {@code :} for simple parameters (e.g., {@code :id:}).
     * Parameters that will receive multiple values should be mapped inside {@code :++:} (e.g., {@code :ids++:}).
     * </p>
     *
     * @return the SQL select statement with named parameters
     */
    String value() default "";

}
