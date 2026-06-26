package br.com.jdbcpp.api;

/**
 * Represents the type of write operation for a {@link Command}.
 * <p>
 * Determines the SQL operation type and influences the valid return types for the annotated method.
 * </p>
 */
public enum CommandType {

    /** Insert operation */
    INSERT,

    /** Update operation */
    UPDATE,

    /** Delete operation */
    DELETE

}
