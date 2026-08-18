package br.com.jdbcpp.processor.service.method;

import br.com.jdbcpp.processor.dto.ParamKind;
import br.com.jdbcpp.processor.dto.result.SetterStrategy;
import br.com.jdbcpp.processor.exception.InvalidSelectResultMappingException;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.TypeUtil;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.extension.Fixture;
import util.extension.FixtureElement;
import util.extension.MicroProcessorExtension;
import util.extension.ProcessingEnv;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, MicroProcessorExtension.class})
@Fixture(
        resourcePath = "service/method/BuildSetterStrategyTest.txt",
        packageName = "com.example"
)
class BuildSetterStrategyTest {

    @Mock
    private CollectionUtil collectionUtil;
    @Mock
    private TypeUtil typeUtil;

    @ProcessingEnv
    private ProcessingEnvironment processingEnv;
    @FixtureElement
    private TypeElement fixture;

    @Test
    void shouldGenerateStrategyInfoForValidSetters() throws Exception {
        final var typeElement = getTypeElement("UserWithValidSetters");
        final var strategy = buildSetterStrategyInstance();

        lenient().when(collectionUtil.isCollectionType(any())).thenReturn(false);
        lenient().when(typeUtil.isNestedObjectType(any())).thenReturn(false);

        final var result = strategy.generateStrategyInfo(typeElement);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst()).isInstanceOf(SetterStrategy.class);
        assertThat(result.getFirst().getName()).isEqualTo("id");
        assertThat(result.getFirst().getParamKind()).isEqualTo(ParamKind.JAVA_TYPE);
        assertThat(result.getFirst().getResultSetIndex()).isNull();
        assertThat(((SetterStrategy)result.getFirst()).getMethodName()).isEqualTo("setId");

        assertThat(result.get(1)).isInstanceOf(SetterStrategy.class);
        assertThat(result.get(1).getName()).isEqualTo("name");
        assertThat(result.get(1).getParamKind()).isEqualTo(ParamKind.JAVA_TYPE);
        assertThat(result.get(1).getResultSetIndex()).isNull();
        assertThat(((SetterStrategy)result.get(1)).getMethodName()).isEqualTo("setName");
    }

    @Test
    void shouldUseCustomSetterWhenSpecified() throws Exception {
        final var typeElement = getTypeElement("UserWithCustomSetter");
        final var strategy = buildSetterStrategyInstance();

        lenient().when(collectionUtil.isCollectionType(any())).thenReturn(false);
        lenient().when(typeUtil.isNestedObjectType(any())).thenReturn(false);

        final var result = strategy.generateStrategyInfo(typeElement);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst()).isInstanceOf(SetterStrategy.class);
        assertThat(result.getFirst().getName()).isEqualTo("id");
        assertThat(((SetterStrategy)result.getFirst()).getMethodName()).isEqualTo("changeId");
    }

    @Test
    void shouldIgnoreFieldMarkedAsIgnored() throws Exception {
        final var typeElement = getTypeElement("UserWithIgnoredField");
        final var strategy = buildSetterStrategyInstance();

        lenient().when(collectionUtil.isCollectionType(any())).thenReturn(false);
        lenient().when(typeUtil.isNestedObjectType(any())).thenReturn(false);

        final var result = strategy.generateStrategyInfo(typeElement);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("id");
    }

    @Test
    void shouldThrowExceptionWhenSetterNotFound() {
        final var typeElement = getTypeElement("UserWithMissingSetter");
        final var strategy = buildSetterStrategyInstance();

        assertThatThrownBy(() -> strategy.generateStrategyInfo(typeElement))
                .isInstanceOf(InvalidSelectResultMappingException.class)
                .hasMessageContaining("no setter found for field 'name'")
                .hasMessageContaining("UserWithMissingSetter");
    }

    @Test
    void shouldUseIndexBasedAccessWhenAllIndexesProvided() throws Exception {
        final var typeElement = getTypeElement("UserWithIndexBasedAccess");
        final var strategy = buildSetterStrategyInstance();

        lenient().when(collectionUtil.isCollectionType(any())).thenReturn(false);
        lenient().when(typeUtil.isNestedObjectType(any())).thenReturn(false);

        final var result = strategy.generateStrategyInfo(typeElement);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getResultSetIndex()).isOne();
        assertThat(result.get(1).getResultSetIndex()).isEqualTo(2);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "UserWithInvalidIndexStart, must have a minimum result set index of 1",
            "UserWithNonSequentialIndexes, not using sequential numbers",
            "UserWithNoFields, No fields found"
    }, delimiter = ',')
    void shouldThrowExceptionForInvalidIndexConfiguration(final String typeElementName,
                                                          final String errorMessage) {
        final var typeElement = getTypeElement(typeElementName);
        final var strategy = buildSetterStrategyInstance();

        assertThatThrownBy(() -> strategy.generateStrategyInfo(typeElement))
                .isInstanceOf(InvalidSelectResultMappingException.class)
                .hasMessageContaining(errorMessage);
    }

    @Test
    void shouldDetermineParamKindAsNestedObject() throws Exception {
        final var typeElement = getTypeElement("UserWithMixedTypes");
        final var strategy = buildSetterStrategyInstance();

        lenient().when(collectionUtil.isCollectionType(any())).thenReturn(false);
        when(typeUtil.isNestedObjectType(any())).thenReturn(true);

        final var result = strategy.generateStrategyInfo(typeElement);

        assertThat(result).hasSize(5);
        assertThat(result.get(2).getParamKind()).isEqualTo(ParamKind.NESTED_OBJECT);
    }

    @Test
    void shouldDetermineParamKindAsCollectionJavaType() throws Exception {
        final var typeElement = getTypeElement("UserWithMixedTypes");
        final var strategy = buildSetterStrategyInstance();

        when(collectionUtil.isCollectionType(any())).thenReturn(true);
        when(collectionUtil.getCollectionElementType(any())).thenReturn(processingEnv.getElementUtils().getTypeElement("java.lang.String").asType());
        when(typeUtil.isNestedObjectType(any())).thenReturn(false);

        final var result = strategy.generateStrategyInfo(typeElement);

        assertThat(result).hasSize(5);
        assertThat(result.get(3).getParamKind()).isEqualTo(ParamKind.COLLECTION_JAVA_TYPE);
        assertThat(result.get(3).getGenericType()).isNotNull();
    }

    @Test
    void shouldDetermineParamKindAsCollectionNested() throws Exception {
        final var typeElement = getTypeElement("UserWithMixedTypes");
        final var addressType = getTypeElement("Address");
        final var strategy = buildSetterStrategyInstance();

        when(collectionUtil.isCollectionType(any())).thenReturn(true);
        when(collectionUtil.getCollectionElementType(any())).thenReturn(addressType.asType());
        when(typeUtil.isNestedObjectType(any())).thenReturn(true);

        final var result = strategy.generateStrategyInfo(typeElement);

        assertThat(result).hasSize(5);
        assertThat(result.get(4).getParamKind()).isEqualTo(ParamKind.COLLECTION_NESTED);
        assertThat(result.get(4).getGenericType()).isNotNull();
    }

    private TypeElement getTypeElement(final String className) {
        return processingEnv.getElementUtils()
                .getTypeElement("com.example.BuildSetterStrategyTest." + className);
    }

    private @NonNull BuildSetterStrategy buildSetterStrategyInstance() {
        return new BuildSetterStrategy(
                processingEnv.getTypeUtils(),
                typeUtil,
                collectionUtil
        );
    }
}