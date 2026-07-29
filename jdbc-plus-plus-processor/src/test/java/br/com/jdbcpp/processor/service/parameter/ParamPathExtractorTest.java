package br.com.jdbcpp.processor.service.parameter;

import br.com.jdbcpp.processor.dto.parameter.ClassParamInfo;
import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import util.extension.Fixture;
import util.extension.FixtureElement;
import util.extension.MicroProcessorExtension;
import util.extension.ProcessingEnv;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MicroProcessorExtension.class)
@Fixture(
        resourcePath = "service/parameter/ParamPathExtractorTest.txt",
        packageName = "com.example"
)
class ParamPathExtractorTest {

    @ProcessingEnv
    private ProcessingEnvironment processingEnv;
    @FixtureElement
    private TypeElement fixture;

    private ParamPathExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new ParamPathExtractor();
    }

    private TypeMirror getStringType() {
        return processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
    }

    private TypeMirror getIntType() {
        return processingEnv.getElementUtils().getTypeElement("java.lang.Integer").asType();
    }

    @Test
    void shouldBuildPathForSingleSimpleParam() {
        final var typeMirror = getStringType();
        final var simpleParam = SimpleParamInfo.builder()
                .withName("name")
                .withType(typeMirror)
                .withCustomEnum(false)
                .withQueryParamName("name")
                .withConvertMethod("getName")
                .build();
        final var root = ClassParamInfo.builder()
                .withName("user")
                .withType(typeMirror)
                .withNestedProperties(List.of(simpleParam))
                .withRecordClass(false)
                .withConvertMethod("getUser")
                .build();

        final var result = extractor.build(root);

        assertThat(result).hasSize(1)
                .containsEntry("name", List.of(root, simpleParam));
    }

    @Test
    void shouldBuildPathForMultipleSimpleParams() {
        final var stringType = getStringType();
        final var intType = getIntType();
        final var nameParam = SimpleParamInfo.builder()
                .withName("name")
                .withType(stringType)
                .withCustomEnum(false)
                .withQueryParamName("name")
                .withConvertMethod("getName")
                .build();
        final var ageParam = SimpleParamInfo.builder()
                .withName("age")
                .withType(intType)
                .withCustomEnum(false)
                .withQueryParamName("age")
                .withConvertMethod("getAge")
                .build();
        final var root = ClassParamInfo.builder()
                .withName("user")
                .withType(stringType)
                .withNestedProperties(List.of(nameParam, ageParam))
                .withRecordClass(false)
                .withConvertMethod("getUser")
                .build();

        final var result = extractor.build(root);

        assertThat(result).hasSize(2)
                .containsEntry("name", List.of(root, nameParam))
                .containsEntry("age", List.of(root, ageParam));
    }

    @Test
    void shouldIgnoreSimpleParamWithIgnoreFlag() {
        final var typeMirror = getStringType();
        final var ignoredParam = SimpleParamInfo.builder()
                .withName("name")
                .withType(typeMirror)
                .withCustomEnum(false)
                .withQueryParamName("name")
                .withConvertMethod("getName")
                .withIgnore(true)
                .build();
        final var root = ClassParamInfo.builder()
                .withName("user")
                .withType(typeMirror)
                .withNestedProperties(List.of(ignoredParam))
                .withRecordClass(false)
                .withConvertMethod("getUser")
                .build();

        final var result = extractor.build(root);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldBuildPathForNestedStructure() {
        final var typeMirror = getStringType();
        final var streetParam = SimpleParamInfo.builder()
                .withName("street")
                .withType(typeMirror)
                .withCustomEnum(false)
                .withQueryParamName("street")
                .withConvertMethod("getStreet")
                .build();
        final var addressClass = ClassParamInfo.builder()
                .withName("address")
                .withType(typeMirror)
                .withNestedProperties(List.of(streetParam))
                .withRecordClass(false)
                .withConvertMethod("getAddress")
                .build();
        final var root = ClassParamInfo.builder()
                .withName("user")
                .withType(typeMirror)
                .withNestedProperties(List.of(addressClass))
                .withRecordClass(false)
                .withConvertMethod("getUser")
                .build();

        final var result = extractor.build(root);

        assertThat(result).hasSize(1)
                .containsEntry("street", List.of(root, addressClass, streetParam));
    }

    @Test
    void shouldBuildPathForMultipleNestedProperties() {
        final var typeMirror = getStringType();
        final var streetParam = SimpleParamInfo.builder()
                .withName("street")
                .withType(typeMirror)
                .withCustomEnum(false)
                .withQueryParamName("street")
                .withConvertMethod("getStreet")
                .build();
        final var cityParam = SimpleParamInfo.builder()
                .withName("city")
                .withType(typeMirror)
                .withCustomEnum(false)
                .withQueryParamName("city")
                .withConvertMethod("getCity")
                .build();
        final var addressClass = ClassParamInfo.builder()
                .withName("address")
                .withType(typeMirror)
                .withNestedProperties(List.of(streetParam, cityParam))
                .withRecordClass(false)
                .withConvertMethod("getAddress")
                .build();
        final var root = ClassParamInfo.builder()
                .withName("user")
                .withType(typeMirror)
                .withNestedProperties(List.of(addressClass))
                .withRecordClass(false)
                .withConvertMethod("getUser")
                .build();

        final var result = extractor.build(root);

        assertThat(result).hasSize(2)
                .containsEntry("street", List.of(root, addressClass, streetParam))
                .containsEntry("city", List.of(root, addressClass, cityParam));
    }

    @Test
    void shouldBuildPathForDeeplyNestedStructure() {
        final var typeMirror = getStringType();
        final var valueParam = SimpleParamInfo.builder()
                .withName("value")
                .withType(typeMirror)
                .withCustomEnum(false)
                .withQueryParamName("value")
                .withConvertMethod("getValue")
                .build();
        final var configClass = ClassParamInfo.builder()
                .withName("config")
                .withType(typeMirror)
                .withNestedProperties(List.of(valueParam))
                .withRecordClass(false)
                .withConvertMethod("getConfig")
                .build();
        final var settingsClass = ClassParamInfo.builder()
                .withName("settings")
                .withType(typeMirror)
                .withNestedProperties(List.of(configClass))
                .withRecordClass(false)
                .withConvertMethod("getSettings")
                .build();
        final var root = ClassParamInfo.builder()
                .withName("user")
                .withType(typeMirror)
                .withNestedProperties(List.of(settingsClass))
                .withRecordClass(false)
                .withConvertMethod("getUser")
                .build();

        final var result = extractor.build(root);

        assertThat(result).hasSize(1)
                .containsEntry("value", List.of(root, settingsClass, configClass, valueParam));
    }

    @Test
    void shouldBuildPathForMixedNestedAndSimpleParams() {
        final var stringType = getStringType();
        final var intType = getIntType();
        final var nameParam = SimpleParamInfo.builder()
                .withName("name")
                .withType(stringType)
                .withCustomEnum(false)
                .withQueryParamName("name")
                .withConvertMethod("getName")
                .build();
        final var streetParam = SimpleParamInfo.builder()
                .withName("street")
                .withType(stringType)
                .withCustomEnum(false)
                .withQueryParamName("street")
                .withConvertMethod("getStreet")
                .build();
        final var addressClass = ClassParamInfo.builder()
                .withName("address")
                .withType(stringType)
                .withNestedProperties(List.of(streetParam))
                .withRecordClass(false)
                .withConvertMethod("getAddress")
                .build();
        final var ageParam = SimpleParamInfo.builder()
                .withName("age")
                .withType(intType)
                .withCustomEnum(false)
                .withQueryParamName("age")
                .withConvertMethod("getAge")
                .build();
        final var root = ClassParamInfo.builder()
                .withName("user")
                .withType(stringType)
                .withNestedProperties(List.of(nameParam, addressClass, ageParam))
                .withRecordClass(false)
                .withConvertMethod("getUser")
                .build();

        final var result = extractor.build(root);

        assertThat(result).hasSize(3)
                .containsEntry("name", List.of(root, nameParam))
                .containsEntry("street", List.of(root, addressClass, streetParam))
                .containsEntry("age", List.of(root, ageParam));
    }

    @Test
    void shouldBuildPathForMultipleNestedClasses() {
        final var typeMirror = getStringType();
        final var streetParam = SimpleParamInfo.builder()
                .withName("street")
                .withType(typeMirror)
                .withCustomEnum(false)
                .withQueryParamName("street")
                .withConvertMethod("getStreet")
                .build();
        final var addressClass = ClassParamInfo.builder()
                .withName("address")
                .withType(typeMirror)
                .withNestedProperties(List.of(streetParam))
                .withRecordClass(false)
                .withConvertMethod("getAddress")
                .build();
        final var phoneParam = SimpleParamInfo.builder()
                .withName("phone")
                .withType(typeMirror)
                .withCustomEnum(false)
                .withQueryParamName("phone")
                .withConvertMethod("getPhone")
                .build();
        final var contactClass = ClassParamInfo.builder()
                .withName("contact")
                .withType(typeMirror)
                .withNestedProperties(List.of(phoneParam))
                .withRecordClass(false)
                .withConvertMethod("getContact")
                .build();
        final var root = ClassParamInfo.builder()
                .withName("user")
                .withType(typeMirror)
                .withNestedProperties(List.of(addressClass, contactClass))
                .withRecordClass(false)
                .withConvertMethod("getUser")
                .build();

        final var result = extractor.build(root);

        assertThat(result).hasSize(2)
                .containsEntry("street", List.of(root, addressClass, streetParam))
                .containsEntry("phone", List.of(root, contactClass, phoneParam));
    }

    @Test
    void shouldReturnEmptyMapForEmptyRoot() {
        final var typeMirror = getStringType();
        final var root = ClassParamInfo.builder()
                .withName("user")
                .withType(typeMirror)
                .withNestedProperties(List.of())
                .withRecordClass(false)
                .withConvertMethod("getUser")
                .build();

        final var result = extractor.build(root);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleIgnoredParamsInNestedStructure() {
        final var typeMirror = getStringType();
        final var ignoredParam = SimpleParamInfo.builder()
                .withName("ignored")
                .withType(typeMirror)
                .withCustomEnum(false)
                .withQueryParamName("ignored")
                .withConvertMethod("getIgnored")
                .withIgnore(true)
                .build();
        final var validParam = SimpleParamInfo.builder()
                .withName("valid")
                .withType(typeMirror)
                .withCustomEnum(false)
                .withQueryParamName("valid")
                .withConvertMethod("getValid")
                .build();
        final var root = ClassParamInfo.builder()
                .withName("user")
                .withType(typeMirror)
                .withNestedProperties(List.of(ignoredParam, validParam))
                .withRecordClass(false)
                .withConvertMethod("getUser")
                .build();

        final var result = extractor.build(root);

        assertThat(result).hasSize(1)
                .containsEntry("valid", List.of(root, validParam));
    }

    @Test
    void shouldHandleIgnoredParamsInNestedClass() {
        final var typeMirror = getStringType();
        final var ignoredParam = SimpleParamInfo.builder()
                .withName("ignored")
                .withType(typeMirror)
                .withCustomEnum(false)
                .withQueryParamName("ignored")
                .withConvertMethod("getIgnored")
                .withIgnore(true)
                .build();
        final var validParam = SimpleParamInfo.builder()
                .withName("valid")
                .withType(typeMirror)
                .withCustomEnum(false)
                .withQueryParamName("valid")
                .withConvertMethod("getValid")
                .build();
        final var nestedClass = ClassParamInfo.builder()
                .withName("address")
                .withType(typeMirror)
                .withNestedProperties(List.of(ignoredParam, validParam))
                .withRecordClass(false)
                .withConvertMethod("getAddress")
                .build();
        final var root = ClassParamInfo.builder()
                .withName("user")
                .withType(typeMirror)
                .withNestedProperties(List.of(nestedClass))
                .withRecordClass(false)
                .withConvertMethod("getUser")
                .build();

        final var result = extractor.build(root);

        assertThat(result).hasSize(1)
                .containsEntry("valid", List.of(root, nestedClass, validParam));
    }

}
