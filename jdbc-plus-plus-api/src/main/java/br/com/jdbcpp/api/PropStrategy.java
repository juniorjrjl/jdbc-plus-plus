package br.com.jdbcpp.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Maps class properties to result set columns for read operations.
 * <p>
 * Used to configure how properties in the result class should be populated from the query result set.
 * Can be applied to method parameters or methods (setters) in the result class.
 * </p>
 */
@Target({PARAMETER, METHOD})
@Retention(SOURCE)
public @interface PropStrategy {

    String value();

    /**
     * The index of the column in the result set.
     * <p>
     * Used when the column should be identified by index instead of name.
     * Default is {@code -1} to indicate that column name should be used instead.
     * </p>
     *
     * @return the result set column index, or {@code -1} to use column name
     */
    int resultSetIndex() default -1;

    String setter();

    /**
     * Whether to ignore this property when building the result.
     *
     * @return {@code true} to ignore this property, {@code false} otherwise
     */
    boolean ignore() default false;

}
