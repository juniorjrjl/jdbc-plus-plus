package br.com.jdbcpp.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static br.com.jdbcpp.api.CommandType.INSERT;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marks methods that should implement write command logic.
 */
@Retention(SOURCE)
@Target(METHOD)
public @interface Command {

    /**
     * The SQL statement to execute.
     * <p>
     * Must contain named parameters prefixed with {@code :} for simple parameters (e.g., {@code :id:}).
     * Parameters that will receive multiple values should be mapped inside {@code :++:} (e.g., {@code :ids++:}).
     * </p>
     *
     * @return the SQL statement with named parameters
     */
    String value() default "";

    /**
     * The type of write operation to perform.
     * <p>
     * Determines the operation type using values from the {@link CommandType} enum.
     * </p>
     * <p>
     * Return type rules by command type:
     * </p>
     * <ul>
     * <li>{@link CommandType#INSERT}: {@code void}, the inserted value (same type as the parameter),
     * or {@code int}/{@code long} for rows affected when {@link #returnRowsAffected()} is {@code true}</li>
     * <li>{@link CommandType#UPDATE}: same rules as INSERT</li>
     * <li>{@link CommandType#DELETE}: {@code void} or {@code int}/{@code long} for rows affected
     * when {@link #returnRowsAffected()} is {@code true}</li>
     * </ul>
     *
     * @return the command type
     */
    CommandType commandType() default INSERT;

    /**
     * Whether the method should return the number of rows affected by the operation.
     * When this option is enabled the method must return int or long.
     * @return {@code true} if the method should return rows affected, {@code false} otherwise
     */
    boolean returnRowsAffected() default false;

}
