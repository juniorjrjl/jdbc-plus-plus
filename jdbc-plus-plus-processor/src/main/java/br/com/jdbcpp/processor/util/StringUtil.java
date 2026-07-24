package br.com.jdbcpp.processor.util;

import java.util.regex.Pattern;

import static java.util.Objects.nonNull;

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
        if (value.isBlank()) {
            return value;
        }

        final var snakePattern = Pattern.compile("^_+|_([a-zA-Z0-9])");
        final var matcher = snakePattern.matcher(value.toLowerCase());

        return matcher.replaceAll(matchResult -> {
            final String group = matchResult.group(1);
            return nonNull(group) ? group.toUpperCase() : "";
        });
    }

}
