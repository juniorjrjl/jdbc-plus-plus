package br.com.jdbcpp.processor.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.FieldUtil;
import util.MicroProcessor;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArrayUtilTest {

    @Mock
    private TypeUtil typeUtil;

    private static Stream<String> shouldIdentifyArrayTypeTrue() {
        return Stream.of(
                "stringArray",
                "intArray"
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldIdentifyArrayTypeTrue(final String fieldName) {
        final var microProcessor = new MicroProcessor<>(
                "util/ArrayUtil/ArrayUtilTest.txt",
                "com.example",
                processingEnv -> new ArrayUtil(typeUtil)
        );
        microProcessor.compile((testInstance, fixture) -> {
            final var type = FieldUtil.getFieldType(fixture, fieldName);

            assertThat(testInstance.isArray(type)).isTrue();
        });
        verifyNoInteractions(typeUtil);
    }

    private static Stream<String> shouldIdentifyArrayTypeFalse() {
        return Stream.of(
                "simpleType"
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldIdentifyArrayTypeFalse(final String fieldName) {
        final var microProcessor = new MicroProcessor<>(
                "util/ArrayUtil/ArrayUtilTest.txt",
                "com.example",
                processingEnv -> new ArrayUtil(typeUtil)
        );
        microProcessor.compile((testInstance, fixture) -> {
            final var type = FieldUtil.getFieldType(fixture, fieldName);

            assertThat(testInstance.isArray(type)).isFalse();
        });
        verifyNoInteractions(typeUtil);
    }

    private static Stream<String> shouldIdentifyNotArrayTypeTrue() {
        return Stream.of(
                "simpleType"
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldIdentifyNotArrayTypeTrue(final String fieldName) {
        final var microProcessor = new MicroProcessor<>(
                "util/ArrayUtil/ArrayUtilTest.txt",
                "com.example",
                processingEnv -> new ArrayUtil(typeUtil)
        );
        microProcessor.compile((testInstance, fixture) -> {
            final var type = FieldUtil.getFieldType(fixture, fieldName);

            assertThat(testInstance.isNotArray(type)).isTrue();
        });
        verifyNoInteractions(typeUtil);
    }

    private static Stream<String> shouldIdentifyNotArrayTypeFalse() {
        return Stream.of(
                "stringArray",
                "intArray"
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldIdentifyNotArrayTypeFalse(final String fieldName) {
        final var microProcessor = new MicroProcessor<>(
                "util/ArrayUtil/ArrayUtilTest.txt",
                "com.example",
                processingEnv -> new ArrayUtil(typeUtil)
        );
        microProcessor.compile((testInstance, fixture) -> {
            final var type = FieldUtil.getFieldType(fixture, fieldName);

            assertThat(testInstance.isNotArray(type)).isFalse();
        });
        verifyNoInteractions(typeUtil);
    }

    private static Stream<Arguments> shouldExtractArrayElementType() {
        return Stream.of(
                Arguments.of("stringArray", "String"),
                Arguments.of("intArray", "int")
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldExtractArrayElementType(final String fieldName, final String expectedElementType) {
        final var microProcessor = new MicroProcessor<>(
                "util/ArrayUtil/ArrayUtilTest.txt",
                "com.example",
                processingEnv -> new ArrayUtil(typeUtil)
        );
        microProcessor.compile((testInstance, fixture) -> {
            final var type = FieldUtil.getFieldType(fixture, fieldName);

            final var elementType = testInstance.getArrayElementType(type);

            assertThat(elementType).isNotNull();
            assertThat(elementType.toString()).contains(expectedElementType);
        });
        verifyNoInteractions(typeUtil);
    }

    @ParameterizedTest
    @MethodSource("shouldIdentifyArrayTypeFalse")
    void shouldReturnNullForNonArrayType(final String fieldName) {
        final var microProcessor = new MicroProcessor<>(
                "util/ArrayUtil/ArrayUtilTest.txt",
                "com.example",
                processingEnv -> new ArrayUtil(typeUtil)
        );
        microProcessor.compile((testInstance, fixture) -> {
            final var type = FieldUtil.getFieldType(fixture, fieldName);

            final var elementType = testInstance.getArrayElementType(type);

            assertThat(elementType).isNull();
        });
        verifyNoInteractions(typeUtil);
    }

    @Test
    void shouldIdentifyArrayOfClass() {
        final var microProcessor = new MicroProcessor<>(
                "util/ArrayUtil/ArrayUtilTest.txt",
                "com.example",
                processingEnv -> new ArrayUtil(typeUtil)
        );
        microProcessor.compile((testInstance, fixture) -> {
            final var stringArray = FieldUtil.getFieldType(fixture, "stringArray");

            final var elementType = testInstance.getArrayElementType(stringArray);
            when(typeUtil.isNotSimpleType(elementType)).thenReturn(true);

            assertThat(testInstance.isArrayOfClass(stringArray)).isTrue();
        });
    }

    @Test
    void shouldReturnFalseForSimpleTypeArray() {
        final var microProcessor = new MicroProcessor<>(
                "util/ArrayUtil/ArrayUtilTest.txt",
                "com.example",
                processingEnv -> new ArrayUtil(typeUtil)
        );
        microProcessor.compile((testInstance, fixture) -> {
            final var stringArray = FieldUtil.getFieldType(fixture, "stringArray");

            final var elementType = testInstance.getArrayElementType(stringArray);
            when(typeUtil.isNotSimpleType(elementType)).thenReturn(false);

            assertThat(testInstance.isArrayOfClass(stringArray)).isFalse();
        });
    }

    @ParameterizedTest
    @MethodSource("shouldIdentifyArrayTypeFalse")
    void shouldReturnFalseForNonArray(final String fieldName) {
        final var microProcessor = new MicroProcessor<>(
                "util/ArrayUtil/ArrayUtilTest.txt",
                "com.example",
                processingEnv -> new ArrayUtil(typeUtil)
        );
        microProcessor.compile((testInstance, fixture) -> {
            final var type = FieldUtil.getFieldType(fixture, fieldName);

            assertThat(testInstance.isArrayOfClass(type)).isFalse();
        });
        verifyNoInteractions(typeUtil);
    }

}
