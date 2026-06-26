package br.com.jdbcpp.api;

/**
 * Strategy for building query result objects.
 * <p>
 * Used in {@link ResultBuildStrategy} to determine how class properties should be populated
 * from the query result set.
 * </p>
 */
public enum ResultBuildStrategyType {

    /** Populates class properties using the constructor */
    CONSTRUCTOR,

    /** Populates class properties using setter methods */
    SETTER,

    /** Returns a simple value (not a class instance) */
    SIMPLE_RESULT
}
