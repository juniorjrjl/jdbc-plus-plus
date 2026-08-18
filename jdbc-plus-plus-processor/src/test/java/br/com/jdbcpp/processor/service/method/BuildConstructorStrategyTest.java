package br.com.jdbcpp.processor.service.method;

import br.com.jdbcpp.processor.dto.ParamKind;
import br.com.jdbcpp.processor.dto.result.ConstructorStrategy;
import br.com.jdbcpp.processor.exception.InvalidSelectResultMappingException;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.TypeUtil;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.extension.Fixture;
import util.extension.FixtureElement;
import util.extension.MicroProcessorExtension;
import util.extension.ProcessingEnv;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, MicroProcessorExtension.class})
@Fixture(
        resourcePath = "service/method/BuildConstructorStrategyTest.txt",
        packageName = "com.example"
)
class BuildConstructorStrategyTest {

    @Mock
    private CollectionUtil collectionUtil;
    @Mock
    private TypeUtil typeUtil;

    @ProcessingEnv
    private ProcessingEnvironment processingEnv;
    @FixtureElement
    private TypeElement fixture;

    @Test
    void shouldGenerateStrategyInfoForValidConstructor() throws Exception {
        final var typeElement = getTypeElement("UserWithConstructor");
        final var strategy = buildConstructorStrategyInstance();

        lenient().when(collectionUtil.isCollectionType(any())).thenReturn(false);
        lenient().when(typeUtil.isNestedObjectType(any())).thenReturn(false);

        final var result = strategy.generateStrategyInfo(typeElement.asType(), "testMethod");

        assertThat(result).hasSize(2);
        assertThat(result.getFirst()).isInstanceOf(ConstructorStrategy.class);
        assertThat(result.getFirst().getName()).isEqualTo("id");
        assertThat(result.getFirst().getParamKind()).isEqualTo(ParamKind.JAVA_TYPE);
        assertThat(result.getFirst().getResultSetIndex()).isOne();
        assertThat(result.get(1).getName()).isEqualTo("name");
        assertThat(result.get(1).getParamKind()).isEqualTo(ParamKind.JAVA_TYPE);
        assertThat(result.get(1).getResultSetIndex()).isEqualTo(2);
    }

    private static final List<Arguments> shouldThrowExceptionWhenUsingInvalidClassConfiguration =
            List.of(
                    Arguments.of("UserWithNoParamConstructor", List.of("UserWithNoParamConstructor", "must have a public constructor with parameters")),
                    Arguments.of("UserWithMultipleConstructors", List.of("testMethod", "UserWithMultipleConstructors", "must have only one constructor"))
            );

    @ParameterizedTest
    @FieldSource
    void shouldThrowExceptionWhenUsingInvalidClassConfiguration(final String typeElementName,
                                                                final List<String> messageFragments) {
        final var typeElement = getTypeElement(typeElementName);
        final var strategy = buildConstructorStrategyInstance();

        final var exceptionAssert =
                assertThatThrownBy(() -> strategy.generateStrategyInfo(typeElement.asType(), "testMethod"))
                .isInstanceOf(InvalidSelectResultMappingException.class);

        messageFragments.forEach(exceptionAssert::hasMessageContaining);
    }

    @Test
    void shouldDetermineParamKindAsNestedObject() throws Exception {
        final var typeElement = getTypeElement("UserWithMixedTypes");
        final var strategy = buildConstructorStrategyInstance();

        lenient().when(collectionUtil.isCollectionType(any())).thenReturn(false);
        when(typeUtil.isNestedObjectType(any())).thenReturn(true);

        final var result = strategy.generateStrategyInfo(typeElement.asType(), "testMethod");

        assertThat(result).hasSize(5);
        assertThat(result.get(2).getParamKind()).isEqualTo(ParamKind.NESTED_OBJECT);
    }

    @Test
    void shouldDetermineParamKindAsCollectionJavaType() throws Exception {
        final var typeElement = getTypeElement("UserWithMixedTypes");
        final var strategy = buildConstructorStrategyInstance();

        when(collectionUtil.isCollectionType(any())).thenReturn(true);
        when(collectionUtil.getCollectionElementType(any())).thenReturn(processingEnv.getElementUtils().getTypeElement("java.lang.String").asType());
        when(typeUtil.isNestedObjectType(any())).thenReturn(false);

        final var result = strategy.generateStrategyInfo(typeElement.asType(), "testMethod");

        assertThat(result).hasSize(5);
        assertThat(result.get(3).getParamKind()).isEqualTo(ParamKind.COLLECTION_JAVA_TYPE);
        assertThat(result.get(3).getGenericType()).isNotNull();
    }

    @Test
    void shouldDetermineParamKindAsCollectionNested() throws Exception {
        final var typeElement = getTypeElement("UserWithMixedTypes");
        final var addressType = getTypeElement("Address");
        final var strategy = buildConstructorStrategyInstance();

        when(collectionUtil.isCollectionType(any())).thenReturn(true);
        when(collectionUtil.getCollectionElementType(any())).thenReturn(addressType.asType());
        when(typeUtil.isNestedObjectType(any())).thenReturn(true);

        final var result = strategy.generateStrategyInfo(typeElement.asType(), "testMethod");

        assertThat(result).hasSize(5);
        assertThat(result.get(4).getParamKind()).isEqualTo(ParamKind.COLLECTION_NESTED);
        assertThat(result.get(4).getGenericType()).isNotNull();
    }

    private TypeElement getTypeElement(final String className) {
        return processingEnv.getElementUtils()
                .getTypeElement("com.example.BuildConstructorStrategyTest." + className);
    }

    private @NonNull BuildConstructorStrategy buildConstructorStrategyInstance() {
        return new BuildConstructorStrategy(
                processingEnv.getTypeUtils(),
                typeUtil,
                collectionUtil
        );
    }

}
