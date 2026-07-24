package br.com.jdbcpp.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Maps method parameters or class properties to statement parameters.
 * <p>
 * Used to configure how parameters received in the method or properties of the received class
 * should be mapped to the named parameters defined in {@link Command} or {@link Query}.
 * </p>
 */
@Retention(SOURCE)
@Target({FIELD, PARAMETER})
public @interface InputParam {

    /**
     * The method name that returns the value of the marked property.
     * <p>
     * Used when the property does not follow the getter pattern or does not have a method
     * with the property name.
     * </p>
     *
     * @return the method name to retrieve the property value
     */
    String value() default "";

    /**
     * Whether to ignore this property.
     * <p>
     * Only applicable to class properties, not to simple method parameters.
     * </p>
     *
     * @return {@code true} to ignore this property, {@code false} otherwise
     */
    boolean ignore() default false;

    /**
     * The name of the parameter defined in the {@link Command} or {@link Query} SQL statement.
     *
     * @return the statement parameter name
     */
    String statementField() default "";

    /**
     * The strategy for extracting enum values.
     *
     * @return the enum value extraction strategy
     */
    String enumMethodValue() default "toString";

}
