package br.com.jdbcpp.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Maps class properties to result set columns for read operations.
 * <p>
 * Used to configure how properties in the result class should be populated from the query result set.
 * Can be applied to method parameters or fields in the result class.
 * </p>
 */
@Target({PARAMETER, FIELD})
@Retention(SOURCE)
public @interface PropStrategy {

    /**
     * The name of a custom setter method to use for setting this property.
     * <p>
     * When using {@link br.com.jdbcpp.api.ResultBuildStrategyType#SETTER},
     * this specifies the method name to invoke instead of the standard JavaBean setter.
     * For example, specifying "changeId" would invoke the {@code changeId()} method
     * instead of the default {@code setId()} method.
     * </p>
     *
     * @return the custom setter method name, or empty string to use the default setter
     */
    String value() default "";

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

    /**
     * Whether to ignore this property when building the result.
     *
     * @return {@code true} to ignore this property, {@code false} otherwise
     */
    boolean ignore() default false;

}
