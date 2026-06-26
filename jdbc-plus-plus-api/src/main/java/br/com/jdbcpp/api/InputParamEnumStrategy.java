package br.com.jdbcpp.api;

/**
 * Strategy for extracting enum values for statement parameters.
 * <p>
 * Used in {@link InputParam} to determine how an enum value should be converted
 * to a parameter value for the SQL statement.
 * </p>
 */
public enum InputParamEnumStrategy {

    /** Extracts the enum value using its ordinal position */
    ORDINAL,

    /** Extracts the enum value using its name as a string */
    STRING,

    /**
     * Extracts the enum value using a custom method.
     * <p>
     * The method name must be specified in the {@link InputParam#value()} property.
     * </p>
     */
    CUSTOM_ENUM_METHOD

}
