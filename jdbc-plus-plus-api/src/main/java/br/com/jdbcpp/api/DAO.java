package br.com.jdbcpp.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marks interfaces or abstract classes to be processed for DAO generation to create a DAO implementation.
 * <p>
 * When applied to an abstract class, the class must have exactly one constructor
 * that accepts {@link javax.sql.DataSource} as one of its parameters or as its only parameter.
 * </p>
 */
@Retention(SOURCE)
@Target(TYPE)
public @interface DAO {
}
