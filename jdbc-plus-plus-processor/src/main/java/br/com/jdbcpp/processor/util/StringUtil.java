package br.com.jdbcpp.processor.util;

import java.util.regex.Pattern;

import static java.util.Objects.isNull;

public final class StringUtil {

    private  StringUtil() {
    }

    public static String toQuotedString(final String value) {
        return "\"" + value + "\"";
    }

    public static String camelToSnakeCase(final String value) {
        return value
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
                .replaceAll("([a-z\\d])([A-Z])", "$1_$2")
                .toLowerCase();
    }

    public static String snakeToCamelCase(final String value) {
        if (value.isEmpty()) {
            return value;
        }

        final var pattern = Pattern.compile("_([a-zA-Z0-9])");
        final var matcher = pattern.matcher(value.toLowerCase());

        return matcher.replaceAll(matchResult -> matchResult.group(1).toUpperCase());
    }

}
