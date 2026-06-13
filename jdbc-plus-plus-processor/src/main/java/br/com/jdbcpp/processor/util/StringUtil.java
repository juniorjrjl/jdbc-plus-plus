package br.com.jdbcpp.processor.util;

public final class StringUtil {

    private  StringUtil() {
    }

    public static String camelToSnakeCase(final String value) {
        return value
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
                .replaceAll("([a-z\\d])([A-Z])", "$1_$2")
                .toLowerCase();
    }

}
