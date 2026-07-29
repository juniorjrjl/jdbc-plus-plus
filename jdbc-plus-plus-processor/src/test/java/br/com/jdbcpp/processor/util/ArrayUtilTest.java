package br.com.jdbcpp.processor.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.FieldUtil;
import util.extension.Fixture;
import util.extension.FixtureElement;
import util.extension.MicroProcessorExtension;
import util.extension.ProcessingEnv;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, MicroProcessorExtension.class})
@Fixture(
        resourcePath = "util/ArrayUtil/ArrayUtilTest.txt",
        packageName = "com.example"
)
class ArrayUtilTest {

    @Mock
    private TypeUtil typeUtil;
    @ProcessingEnv
    private ProcessingEnvironment processingEnv;
    @FixtureElement
    private TypeElement fixture;

    private ArrayUtil createArrayUtil() {
        return new ArrayUtil(typeUtil);
    }


    @ParameterizedTest
    @ValueSource(strings = {"stringArray", "intArray"})
    void shouldIdentifyArrayTypeTrue(final String fieldName) {
        final var arrayUtil = createArrayUtil();
        final var type = FieldUtil.getFieldType(fixture, fieldName);
        assertThat(arrayUtil.isArray(type)).isTrue();
        verifyNoInteractions(typeUtil);
    }


    @Test
    void shouldIdentifyArrayTypeFalse() {
        final var arrayUtil = createArrayUtil();
        final var type = FieldUtil.getFieldType(fixture, "simpleType");
        assertThat(arrayUtil.isArray(type)).isFalse();
        verifyNoInteractions(typeUtil);
    }

    @Test
    void shouldIdentifyNotArrayTypeTrue() {
        final var arrayUtil = createArrayUtil();
        final var type = FieldUtil.getFieldType(fixture, "simpleType");
        assertThat(arrayUtil.isNotArray(type)).isTrue();
        verifyNoInteractions(typeUtil);
    }

    @ParameterizedTest
    @ValueSource(strings = {"stringArray", "intArray"})
    void shouldIdentifyNotArrayTypeFalse(final String fieldName) {
        final var arrayUtil = createArrayUtil();
        final var type = FieldUtil.getFieldType(fixture, fieldName);
        assertThat(arrayUtil.isNotArray(type)).isFalse();
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
        final var arrayUtil = createArrayUtil();
        final var type = FieldUtil.getFieldType(fixture, fieldName);
        final var elementType = arrayUtil.getArrayElementType(type);
        assertThat(elementType).isNotNull();
        assertThat(elementType.toString()).contains(expectedElementType);
        verifyNoInteractions(typeUtil);
    }

    @Test
    void shouldReturnNullForNonArrayType() {
        final var arrayUtil = createArrayUtil();
        final var type = FieldUtil.getFieldType(fixture, "simpleType");
        final var elementType = arrayUtil.getArrayElementType(type);
        assertThat(elementType).isNull();
        verifyNoInteractions(typeUtil);
    }

    @Test
    void shouldIdentifyArrayOfClass() {
        final var arrayUtil = createArrayUtil();
        final var stringArray = FieldUtil.getFieldType(fixture, "stringArray");
        final var elementType = arrayUtil.getArrayElementType(stringArray);
        when(typeUtil.isNotSimpleType(elementType)).thenReturn(true);
        assertThat(arrayUtil.isArrayOfClass(stringArray)).isTrue();
    }

    @Test
    void shouldReturnFalseForSimpleTypeArray() {
        final var arrayUtil = createArrayUtil();
        final var stringArray = FieldUtil.getFieldType(fixture, "stringArray");
        final var elementType = arrayUtil.getArrayElementType(stringArray);
        when(typeUtil.isNotSimpleType(elementType)).thenReturn(false);
        assertThat(arrayUtil.isArrayOfClass(stringArray)).isFalse();
    }

    @Test
    void shouldReturnFalseForNonArray() {
        final var arrayUtil = createArrayUtil();
        final var type = FieldUtil.getFieldType(fixture, "simpleType");
        assertThat(arrayUtil.isArrayOfClass(type)).isFalse();
        verifyNoInteractions(typeUtil);
    }

}
