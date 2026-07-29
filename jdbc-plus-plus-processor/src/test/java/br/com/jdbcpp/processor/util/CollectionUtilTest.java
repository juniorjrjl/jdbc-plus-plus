package br.com.jdbcpp.processor.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import util.FieldUtil;
import util.extension.Fixture;
import util.extension.FixtureElement;
import util.extension.MicroProcessorExtension;
import util.extension.ProcessingEnv;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MicroProcessorExtension.class)
@Fixture(
        resourcePath = "util/CollectionUtil/CollectionUtilTest.txt",
        packageName = "com.example"
)
class CollectionUtilTest {

    @ProcessingEnv
    private ProcessingEnvironment processingEnv;
    @FixtureElement
    private TypeElement fixture;

    private CollectionUtil createCollectionUtil() {
        return new CollectionUtil(processingEnv.getTypeUtils());
    }

    @ParameterizedTest
    @ValueSource(strings = { "listType", "setType", "arrayListType" })
    void shouldIdentifyCollectionTypeTrue(final String fieldName) {
        final var collectionUtil = createCollectionUtil();
        final var type = FieldUtil.getFieldType(fixture, fieldName);
        assertThat(collectionUtil.isCollectionType(type)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"simpleType", "typeParameterField"} )
    void shouldIdentifyCollectionTypeFalse(final String fieldName) {
        final var collectionUtil = createCollectionUtil();
        final var type = FieldUtil.getFieldType(fixture, fieldName);
        assertThat(collectionUtil.isCollectionType(type)).isFalse();
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
        final var collectionUtil = createCollectionUtil();
        final var type = FieldUtil.getFieldType(fixture, fieldName);
        final var implementation = collectionUtil.getCollectionImplementation(type);
        assertThat(implementation).isEqualTo(expectedImplementation);
    }

    @Test
    void shouldExtractCollectionElementType() {
        final var collectionUtil = createCollectionUtil();
        final var listType = FieldUtil.getFieldType(fixture, "listType");
        final var elementType = collectionUtil.getCollectionElementType(listType);
        assertThat(elementType).isNotNull();
        assertThat(elementType.toString()).contains("String");
    }

    @ParameterizedTest
    @ValueSource(strings = {"typeParameterField", "rawListType"})
    void shouldReturnNullForInvalidCollectionTypes(final String fieldName) {
        final var collectionUtil = createCollectionUtil();
        final var type = FieldUtil.getFieldType(fixture, fieldName);
        final var elementType = collectionUtil.getCollectionElementType(type);
        assertThat(elementType).isNull();
    }

}
