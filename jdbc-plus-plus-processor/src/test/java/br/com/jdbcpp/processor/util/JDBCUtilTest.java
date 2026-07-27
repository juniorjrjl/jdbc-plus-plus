package br.com.jdbcpp.processor.util;

import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JDBCUtilTest {

    private static Stream<Arguments> shouldGetPrepareStatementSetter() {
        return Stream.of(
                Arguments.of(int.class, "ps.setInt(1, fieldName)"),
                Arguments.of(long.class, "ps.setLong(1, fieldName)"),
                Arguments.of(double.class, "ps.setDouble(1, fieldName)"),
                Arguments.of(float.class, "ps.setFloat(1, fieldName)"),
                Arguments.of(boolean.class, "ps.setBoolean(1, fieldName)"),
                Arguments.of(short.class, "ps.setShort(1, fieldName)"),
                Arguments.of(byte.class, "ps.setByte(1, fieldName)"),
                Arguments.of(String.class, "ps.setString(1, fieldName)"),
                Arguments.of(BigDecimal.class, "ps.setBigDecimal(1, fieldName)"),
                Arguments.of(Date.class, "ps.setDate(1, fieldName)"),
                Arguments.of(Time.class, "ps.setTime(1, fieldName)"),
                Arguments.of(Timestamp.class, "ps.setTimestamp(1, fieldName)"),
                Arguments.of(byte[].class, "ps.setBytes(1, fieldName)"),
                Arguments.of(UUID.class, "ps.setObject(1, fieldName)")
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldGetPrepareStatementSetter(final Class<?> type, final String expected) {
        final var result = JDBCUtil.getPrepareStatementSetter(
                "fieldName",
                TypeName.get(type),
                "ps",
                "1"
        );
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> shouldGetResultSetGetter() {
        return Stream.of(
                Arguments.of(int.class, "rsFieldName"),
                Arguments.of(Integer.class, "rsFieldName"),
                Arguments.of(long.class, "rsFieldName"),
                Arguments.of(Long.class, "rsFieldName"),
                Arguments.of(double.class, "rsFieldName"),
                Arguments.of(Double.class, "rsFieldName"),
                Arguments.of(float.class, "rsFieldName"),
                Arguments.of(Float.class, "rsFieldName"),
                Arguments.of(boolean.class, "rsFieldName"),
                Arguments.of(Boolean.class, "rsFieldName"),
                Arguments.of(short.class, "rsFieldName"),
                Arguments.of(Short.class, "rsFieldName"),
                Arguments.of(byte.class, "rsFieldName"),
                Arguments.of(Byte.class, "rsFieldName"),
                Arguments.of(String.class, "rsFieldName"),
                Arguments.of(BigDecimal.class, "rsFieldName"),
                Arguments.of(Date.class, "rsFieldName"),
                Arguments.of(Time.class, "rsFieldName"),
                Arguments.of(Timestamp.class, "rsFieldName"),
                Arguments.of(byte[].class, "rsFieldName"),
                Arguments.of(UUID.class, "rsFieldName")
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldGetResultSetGetter(final Class<?> type, final String expectedVarName) {
        final var builder = MethodSpec.methodBuilder("testMethod");
        final var result = JDBCUtil.getResultSetGetter(
                TypeName.get(type),
                "\"column\"",
                "rs",
                "fieldName",
                true,
                builder
        );
        assertThat(result).isEqualTo(expectedVarName);
    }

    private static Stream<Arguments> shouldGetResultSetGetterWithoutPrefix() {
        return Stream.of(
                Arguments.of(int.class, "fieldName"),
                Arguments.of(Integer.class, "fieldName"),
                Arguments.of(long.class, "fieldName"),
                Arguments.of(Long.class, "fieldName"),
                Arguments.of(double.class, "fieldName"),
                Arguments.of(Double.class, "fieldName"),
                Arguments.of(float.class, "fieldName"),
                Arguments.of(Float.class, "fieldName"),
                Arguments.of(boolean.class, "fieldName"),
                Arguments.of(Boolean.class, "fieldName"),
                Arguments.of(short.class, "fieldName"),
                Arguments.of(Short.class, "fieldName"),
                Arguments.of(byte.class, "fieldName"),
                Arguments.of(Byte.class, "fieldName"),
                Arguments.of(String.class, "fieldName"),
                Arguments.of(BigDecimal.class, "fieldName"),
                Arguments.of(Date.class, "fieldName"),
                Arguments.of(Time.class, "fieldName"),
                Arguments.of(Timestamp.class, "fieldName"),
                Arguments.of(byte[].class, "fieldName"),
                Arguments.of(UUID.class, "fieldName")
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldGetResultSetGetterWithoutPrefix(final Class<?> type, final String expectedVarName) {
        final var builder = MethodSpec.methodBuilder("testMethod");
        final var result = JDBCUtil.getResultSetGetter(
                TypeName.get(type),
                "\"column\"",
                "rs",
                "fieldName",
                false,
                builder
        );
        assertThat(result).isEqualTo(expectedVarName);
    }

}
