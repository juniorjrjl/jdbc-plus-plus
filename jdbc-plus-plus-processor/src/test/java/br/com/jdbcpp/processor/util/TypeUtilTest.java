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
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, MicroProcessorExtension.class})
@Fixture(
        resourcePath = "util/TypeUtil/TypeUtilTest.txt",
        packageName = "com.example"
)
class TypeUtilTest {

    @Mock
    private CollectionUtil collectionUtil;
    @ProcessingEnv
    private ProcessingEnvironment processingEnv;
    @FixtureElement
    private TypeElement fixture;

    private TypeUtil createTypeUtil() {
        return new TypeUtil(
                processingEnv.getElementUtils(),
                processingEnv.getTypeUtils(),
                collectionUtil
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "primitiveBoolean",
            "primitiveByte",
            "primitiveShort",
            "primitiveInt",
            "primitiveLong",
            "primitiveChar",
            "primitiveFloat",
            "primitiveDouble",
            "wrapperBoolean",
            "wrapperByte",
            "wrapperShort",
            "wrapperInt",
            "wrapperLong",
            "wrapperChar",
            "wrapperFloat",
            "wrapperDouble",
            "enumType",
            "bigDecimalType",
            "bitIntegerType",
            "dateType",
            "uuidType",
            "instantType",
            "localDateType",
            "localDateTimeType",
            "localTimeType",
            "offsetDateTimeType",
            "offsetTimeType",
            "zonedDateTimeType"
    })
    void whenNotPrimitiveTypeThenReturnTrue(final String fieldName) {
        final var typeUtil = createTypeUtil();
        final var primitiveInt = FieldUtil.getFieldType(fixture, fieldName);
        assertThat(typeUtil.isNotSimpleType(primitiveInt)).isFalse();
        verifyNoInteractions(collectionUtil);
    }

    @Test
    void shouldReturnFalseForNonTypeElementCallingIsNotSimpleType() {
        final var typeUtil = createTypeUtil();
        final var typeParameterField = FieldUtil.getFieldType(fixture, "typeParameterField");
        assertThat(typeUtil.isNotSimpleType(typeParameterField)).isTrue();
        verifyNoInteractions(collectionUtil);
    }

    private static Stream<Arguments> shouldSayIfIsEnum() {
        return Stream.of(
                Arguments.of("enumType", true),
                Arguments.of("primitiveBoolean", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldSayIfIsEnum(final String fieldName, final boolean isEnum) {
        final var typeUtil = createTypeUtil();
        final var enumType = FieldUtil.getFieldType(fixture, fieldName);
        assertThat(typeUtil.isEnum(enumType)).isEqualTo(isEnum);
        verifyNoInteractions(collectionUtil);
    }

    private static Stream<Arguments> shouldIdentifyNestedObjectType() {
        return Stream.of(
                Arguments.of("complexEntity", true),
                Arguments.of("listType", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldIdentifyNestedObjectType(final String fieldName, final boolean isNested) {
        final var typeUtil = createTypeUtil();
        final var type = FieldUtil.getFieldType(fixture, fieldName);
        when(collectionUtil.isCollectionType(type)).thenReturn(fieldName.equals("listType"));
        assertThat(typeUtil.isNestedObjectType(type)).isEqualTo(isNested);
    }

    @Test
    void shouldReturnFalseForSimpleTypes() {
        final var typeUtil = createTypeUtil();
        final var wrapperInt = FieldUtil.getFieldType(fixture, "wrapperInt");
        assertThat(typeUtil.isNestedObjectType(wrapperInt)).isFalse();
        verifyNoInteractions(collectionUtil);
    }

    private static Stream<Arguments> shouldIdentifyOptionalType() {
        return Stream.of(
                Arguments.of("optionalString", true),
                Arguments.of("optionalInteger", true),
                Arguments.of("wrapperInt", false),
                Arguments.of("typeParameterField", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldIdentifyOptionalType(final String fieldName, final boolean isOptional) {
        final var typeUtil = createTypeUtil();
        final var type = FieldUtil.getFieldType(fixture, fieldName);
        assertThat(typeUtil.isOptionalType(type)).isEqualTo(isOptional);
        verifyNoInteractions(collectionUtil);
    }

    private static Stream<Arguments> shouldExtractOptionalTypeArgument() {
        return Stream.of(
                Arguments.of("optionalString", "String"),
                Arguments.of("optionalInteger", "Integer")
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldExtractOptionalTypeArgument(final String fieldName, final String expectedTypeName) {
        final var typeUtil = createTypeUtil();
        final var type = FieldUtil.getFieldType(fixture, fieldName);
        final var extractedType = typeUtil.getOptionalType(type);
        assertThat(extractedType).isNotNull();
        assertThat(extractedType.toString()).contains(expectedTypeName);
        verifyNoInteractions(collectionUtil);
    }

    @Test
    void shouldReturnNullForNonOptionalTypes() {
        final var typeUtil = createTypeUtil();
        final var wrapperInt = FieldUtil.getFieldType(fixture, "wrapperInt");
        final var extractedType = typeUtil.getOptionalType(wrapperInt);
        assertThat(extractedType).isNull();
        verifyNoInteractions(collectionUtil);
    }

    @Test
    void shouldReturnNullForOptionalWithoutTypeArgument() {
        final var typeUtil = createTypeUtil();
        final var emptyOptional = FieldUtil.getFieldType(fixture, "emptyOptional");
        final var extractedType = typeUtil.getOptionalType(emptyOptional);
        assertThat(extractedType).isNull();
        verifyNoInteractions(collectionUtil);
    }

    private static Stream<Arguments> shouldIdentifyRecordType() {
        return Stream.of(
                Arguments.of("personRecord", true),
                Arguments.of("complexEntity", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldIdentifyRecordType(final String fieldName, final boolean isRecord) {
        final var typeUtil = createTypeUtil();
        final var type = FieldUtil.getFieldType(fixture, fieldName);
        assertThat(typeUtil.isRecord(type)).isEqualTo(isRecord);
        verifyNoInteractions(collectionUtil);
    }

    @Test
    void shouldReturnFalseForNonTypeElementCallingIsRecord() {
        final var typeUtil = createTypeUtil();
        final var typeParameterField = FieldUtil.getFieldType(fixture, "typeParameterField");
        assertThat(typeUtil.isRecord(typeParameterField)).isFalse();
        verifyNoInteractions(collectionUtil);
    }

    private static Stream<Arguments> shouldBuildContainerTypeMirror() {
        return Stream.of(
                Arguments.of(List.class, "java.util.List", "wrapperInt"),
                Arguments.of(Set.class, "java.util.Set", "primitiveInt")
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldBuildContainerTypeMirror(final Class<? extends Collection> containerType,
                                        final String expectedContainerName,
                                        final String fieldName) {
        final var typeUtil = createTypeUtil();
        final var elementType = FieldUtil.getFieldType(fixture, fieldName);
        final var containerTypeMirror = typeUtil.buildContainerTypeMirror(() -> containerType, elementType);
        assertThat(containerTypeMirror).isNotNull();
        assertThat(containerTypeMirror.toString()).contains(expectedContainerName);
        assertThat(containerTypeMirror.toString()).contains("Integer");
        verifyNoInteractions(collectionUtil);
    }

    private static Stream<Arguments> shouldIdentifyListType() {
        return Stream.of(
                Arguments.of("listType", true),
                Arguments.of("wrapperInt", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldIdentifyListType(final String fieldName, final boolean isList) {
        final var typeUtil = createTypeUtil();
        final var type = FieldUtil.getFieldType(fixture, fieldName);
        assertThat(typeUtil.isList(type)).isEqualTo(isList);
        verifyNoInteractions(collectionUtil);
    }

    private static Stream<Arguments> shouldGetTypeMirrorFromClass() {
        final Supplier<Class<?>> strSupplier = () -> String.class;
        final Supplier<Class<?>> intSupplier = () -> Integer.class;
        final Supplier<Class<?>> bigDecimalSupplier = () -> BigDecimal.class;
        return Stream.of(
                Arguments.of(strSupplier, "java.lang.String"),
                Arguments.of(intSupplier, "java.lang.Integer"),
                Arguments.of(bigDecimalSupplier, "java.math.BigDecimal")
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldGetTypeMirrorFromClass(final Supplier<Class<?>> classCallback, final String expectedTypeName) {
        final var typeUtil = createTypeUtil();
        final var typeMirror = typeUtil.getTypeMirrorFromClass(classCallback);
        assertThat(typeMirror).isNotNull();
        assertThat(typeMirror.toString()).contains(expectedTypeName);
        verifyNoInteractions(collectionUtil);
    }

    @Test
    void shouldIdentifyCollectionOfClass() {
        final var typeUtil = createTypeUtil();
        final var listType = FieldUtil.getFieldType(fixture, "listType");
        final var complexEntity = FieldUtil.getFieldType(fixture, "complexEntity");
        when(collectionUtil.getCollectionElementType(listType)).thenReturn(complexEntity);
        assertThat(typeUtil.isCollectionOfClass(listType)).isTrue();
    }

    @Test
    void shouldReturnFalseForCollectionWithSimpleType() {
        final var typeUtil = createTypeUtil();
        final var listType = FieldUtil.getFieldType(fixture, "listType");
        final var wrapperInt = FieldUtil.getFieldType(fixture, "wrapperInt");
        when(collectionUtil.getCollectionElementType(listType)).thenReturn(wrapperInt);
        assertThat(typeUtil.isCollectionOfClass(listType)).isFalse();
    }

    @Test
    void shouldReturnFalseForNonCollection() {
        final var typeUtil = createTypeUtil();
        final var wrapperInt = FieldUtil.getFieldType(fixture, "wrapperInt");
        when(collectionUtil.getCollectionElementType(wrapperInt)).thenReturn(null);
        assertThat(typeUtil.isCollectionOfClass(wrapperInt)).isFalse();
    }

}