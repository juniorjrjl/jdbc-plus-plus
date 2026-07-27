package br.com.jdbcpp.processor.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilTest {

    private static Stream<Arguments> shouldConvertToQuotedString() {
        return Stream.of(
                Arguments.of("test", "\"test\""),
                Arguments.of("hello world", "\"hello world\""),
                Arguments.of("", "\"\""),
                Arguments.of("123", "\"123\"")
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldConvertToQuotedString(final String value, final String expected) {
        final var result = StringUtil.toQuotedString(value);
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> shouldConvertCamelToSnakeCase() {
        return Stream.of(
                Arguments.of("camelCase", "camel_case"),
                Arguments.of("CamelCase", "camel_case"),
                Arguments.of("camelCaseString", "camel_case_string"),
                Arguments.of("XMLHttpRequest", "xml_http_request"),
                Arguments.of("XML", "xml"),
                Arguments.of("simple", "simple"),
                Arguments.of("already_snake", "already_snake")
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldConvertCamelToSnakeCase(final String value, final String expected) {
        final var result = StringUtil.camelToSnakeCase(value);
        assertThat(result).isEqualTo(expected);
    }

}