package br.com.jdbcpp.processor.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import util.FieldUtil;
import util.MicroProcessor;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionUtilTest {

    private static Stream<String> shouldIdentifyCollectionTypeTrue() {
        return Stream.of(
                "listType",
                "setType",
                "arrayListType"
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldIdentifyCollectionTypeTrue(final String fieldName) {
        final var microProcessor = new MicroProcessor<>(
                "util/CollectionUtil/CollectionUtilTest.txt",
                "com.example",
                processingEnv -> new CollectionUtil(processingEnv.getTypeUtils())
        );
        microProcessor.compile((testInstance, fixture) -> {
            final var type = FieldUtil.getFieldType(fixture, fieldName);

            assertThat(testInstance.isCollectionType(type)).isTrue();
        });
    }

    private static Stream<String> shouldIdentifyCollectionTypeFalse() {
        return Stream.of(
                "simpleType",
                "typeParameterField"
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldIdentifyCollectionTypeFalse(final String fieldName) {
        final var microProcessor = new MicroProcessor<>(
                "util/CollectionUtil/CollectionUtilTest.txt",
                "com.example",
                processingEnv -> new CollectionUtil(processingEnv.getTypeUtils())
        );
        microProcessor.compile((testInstance, fixture) -> {
            final var type = FieldUtil.getFieldType(fixture, fieldName);

            assertThat(testInstance.isCollectionType(type)).isFalse();
        });
    }

    private static Stream<Arguments> shouldGetCollectionImplementation() {
        return Stream.of(
                Arguments.of("listType", "java.util.ArrayList"),
                Arguments.of("setType", "java.util.HashSet"),
                Arguments.of("arrayListType", "java.util.ArrayList"),
                Arguments.of("typeParameterField", "java.util.ArrayList")
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldGetCollectionImplementation(final String fieldName, final String expectedImplementation) {
        final var microProcessor = new MicroProcessor<>(
                "util/CollectionUtil/CollectionUtilTest.txt",
                "com.example",
                processingEnv -> new CollectionUtil(processingEnv.getTypeUtils())
        );
        microProcessor.compile((testInstance, fixture) -> {
            final var type = FieldUtil.getFieldType(fixture, fieldName);

            final var implementation = testInstance.getCollectionImplementation(type);

            assertThat(implementation).isEqualTo(expectedImplementation);
        });
    }

    @Test
    void shouldExtractCollectionElementType() {
        final var microProcessor = new MicroProcessor<>(
                "util/CollectionUtil/CollectionUtilTest.txt",
                "com.example",
                processingEnv -> new CollectionUtil(processingEnv.getTypeUtils())
        );
        microProcessor.compile((testInstance, fixture) -> {
            final var listType = FieldUtil.getFieldType(fixture, "listType");

            final var elementType = testInstance.getCollectionElementType(listType);

            assertThat(elementType).isNotNull();
            assertThat(elementType.toString()).contains("String");
        });
    }

    private static Stream<Arguments> shouldReturnNullForInvalidCollectionTypes() {
        return Stream.of(
                Arguments.of("typeParameterField"),
                Arguments.of("rawListType")
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldReturnNullForInvalidCollectionTypes(final String fieldName) {
        final var microProcessor = new MicroProcessor<>(
                "util/CollectionUtil/CollectionUtilTest.txt",
                "com.example",
                processingEnv -> new CollectionUtil(processingEnv.getTypeUtils())
        );
        microProcessor.compile((testInstance, fixture) -> {
            final var type = FieldUtil.getFieldType(fixture, fieldName);

            final var elementType = testInstance.getCollectionElementType(type);

            assertThat(elementType).isNull();
        });
    }

}
