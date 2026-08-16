package br.com.jdbcpp.processor.service.parameter;

import br.com.jdbcpp.processor.dto.parameter.ClassParamInfo;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfo;
import br.com.jdbcpp.processor.exception.InvalidInputParamException;
import br.com.jdbcpp.processor.util.ArrayUtil;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.TypeUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.extension.Fixture;
import util.extension.FixtureElement;
import util.extension.MicroProcessorExtension;
import util.extension.ProcessingEnv;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class, MicroProcessorExtension.class})
@Fixture(
        resourcePath = "service/parameter/ParameterInfoDelegatorTest.txt",
        packageName = "com.example"
)
class ParameterInfoDelegatorTest {

    @Mock
    private SimpleParamInfoFactory simpleParamInfoFactory;
    @Mock
    private ClassParamInfoFactory classParamInfoFactory;
    @Mock
    private ArrayUtil arrayUtil;
    @Mock
    private CollectionUtil collectionUtil;
    @Mock
    private TypeUtil typeUtil;
    @ProcessingEnv
    private ProcessingEnvironment processingEnv;
    @FixtureElement
    private TypeElement fixture;

    private ParameterInfoDelegator createDelegator() {
        return new ParameterInfoDelegator(
                simpleParamInfoFactory,
                classParamInfoFactory,
                arrayUtil,
                collectionUtil,
                typeUtil
        );
    }

    @Test
    void shouldReturnEmptyListForNoParams() throws Exception {
        final var delegator = createDelegator();
        final var method = getMethod("noParams");

        final var result = delegator.create(method);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldDelegateToSimpleFactoryForSingleSimpleParam() throws Exception {
        final var delegator = createDelegator();
        final var method = getMethod("singleSimpleParam");
        final var stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
        final var simpleParam = SimpleParamInfo.builder()
                .withName("name")
                .withType(stringType)
                .withCustomEnum(false)
                .withQueryParamName("name")
                .withConvertMethod("name")
                .build();

        lenient().when(arrayUtil.isArray(any())).thenReturn(false);
        lenient().when(collectionUtil.isCollectionType(any())).thenReturn(false);
        lenient().when(typeUtil.isNotSimpleType(any())).thenReturn(false);
        lenient().when(simpleParamInfoFactory.create(method)).thenReturn(List.of(simpleParam));

        final var result = delegator.create(method);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isInstanceOf(SimpleParamInfo.class);
        verify(simpleParamInfoFactory).create(method);
    }

    @Test
    void shouldDelegateToSimpleFactoryForMultipleSimpleParams() throws Exception {
        final var delegator = createDelegator();
        final var method = getMethod("multipleSimpleParams");
        final var stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
        final var intType = processingEnv.getElementUtils().getTypeElement("java.lang.Integer").asType();
        final List<ParamInfo> simpleParams = List.of(
                SimpleParamInfo.builder()
                        .withName("name")
                        .withType(stringType)
                        .withCustomEnum(false)
                        .withQueryParamName("name")
                        .withConvertMethod("name")
                        .build(),
                SimpleParamInfo.builder()
                        .withName("age")
                        .withType(intType)
                        .withCustomEnum(false)
                        .withQueryParamName("age")
                        .withConvertMethod("age")
                        .build(),
                SimpleParamInfo.builder()
                        .withName("email")
                        .withType(stringType)
                        .withCustomEnum(false)
                        .withQueryParamName("email")
                        .withConvertMethod("email")
                        .build()
        );

        lenient().when(arrayUtil.isArray(any())).thenReturn(false);
        lenient().when(collectionUtil.isCollectionType(any())).thenReturn(false);
        lenient().when(typeUtil.isNotSimpleType(any())).thenReturn(false);
        lenient().when(simpleParamInfoFactory.create(method)).thenReturn(simpleParams);

        final var result = delegator.create(method);

        assertThat(result).hasSize(3);
        verify(simpleParamInfoFactory).create(method);
    }

    @Test
    void shouldDelegateToClassFactoryForSingleClassParam() throws Exception {
        final var delegator = createDelegator();
        final var method = getMethod("singleClassParam");
        final var personType = processingEnv.getElementUtils()
                .getTypeElement("com.example.ParameterInfoDelegatorTest.Person").asType();
        final var classParam = ClassParamInfo.builder()
                .withName("person")
                .withType(personType)
                .withNestedProperties(List.of())
                .withRecordClass(false)
                .withConvertMethod("person")
                .build();

        lenient().when(arrayUtil.isArray(any())).thenReturn(false);
        lenient().when(collectionUtil.isCollectionType(any())).thenReturn(false);
        lenient().when(typeUtil.isNotSimpleType(personType)).thenReturn(true);
        lenient().when(classParamInfoFactory.create(any())).thenReturn(List.of(classParam));

        final var result = delegator.create(method);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isInstanceOf(ClassParamInfo.class);
        verify(classParamInfoFactory).create(any());
    }

    @Test
    void shouldThrowExceptionForMultipleClassParams() {
        final var delegator = createDelegator();
        final var method = getMethod("multipleClassParams");
        final var personType = processingEnv.getElementUtils()
                .getTypeElement("com.example.ParameterInfoDelegatorTest.Person").asType();
        final var addressType = processingEnv.getElementUtils()
                .getTypeElement("com.example.ParameterInfoDelegatorTest.Address").asType();
        
        lenient().when(arrayUtil.isArray(any())).thenReturn(false);
        lenient().when(collectionUtil.isCollectionType(any())).thenReturn(false);
        lenient().when(typeUtil.isNotSimpleType(personType)).thenReturn(true);
        lenient().when(typeUtil.isNotSimpleType(addressType)).thenReturn(true);

        assertThatThrownBy(() -> delegator.create(method))
                .isInstanceOf(InvalidInputParamException.class)
                .hasMessageContaining("must receive 1 class param or many simple type params");
    }

    @Test
    void shouldThrowExceptionForSimpleParamWithIgnore() {
        final var delegator = createDelegator();
        final var method = getMethod("simpleParamWithIgnore");
        
        lenient().when(arrayUtil.isArray(any())).thenReturn(false);
        lenient().when(collectionUtil.isCollectionType(any())).thenReturn(false);
        lenient().when(typeUtil.isNotSimpleType(any())).thenReturn(false);

        assertThatThrownBy(() -> delegator.create(method))
                .isInstanceOf(InvalidInputParamException.class)
                .hasMessageContaining("can only be used on class properties, not on direct method parameters");
    }

    @Test
    void shouldDelegateToClassFactoryForClassParamInCollection() throws Exception {
        final var delegator = createDelegator();
        final var method = getMethod("classParamInCollection");
        final var personType = processingEnv.getElementUtils()
                .getTypeElement("com.example.ParameterInfoDelegatorTest.Person").asType();

        lenient().when(arrayUtil.isArray(any())).thenReturn(false);
        lenient().when(collectionUtil.isCollectionType(any())).thenReturn(true);
        lenient().when(typeUtil.isCollectionOfClass(any())).thenReturn(true);
        final var classParam = ClassParamInfo.builder()
                .withName("people")
                .withType(personType)
                .withNestedProperties(List.of())
                .withRecordClass(false)
                .withConvertMethod("people")
                .build();
        lenient().when(classParamInfoFactory.create(any())).thenReturn(List.of(classParam));

        final var result = delegator.create(method);

        assertThat(result).hasSize(1);
        verify(classParamInfoFactory).create(any());
    }

    @Test
    void shouldDelegateToClassFactoryForClassParamInArray() throws Exception {
        final var delegator = createDelegator();
        final var method = getMethod("classParamInArray");
        final var personType = processingEnv.getElementUtils()
                .getTypeElement("com.example.ParameterInfoDelegatorTest.Person").asType();
        final var personArray = processingEnv.getTypeUtils().getArrayType(personType);

        lenient().when(arrayUtil.isArray(personArray)).thenReturn(true);
        lenient().when(arrayUtil.isArrayOfClass(personArray)).thenReturn(true);
        lenient().when(collectionUtil.isCollectionType(any())).thenReturn(false);
        final var classParam = ClassParamInfo.builder()
                .withName("people")
                .withType(personType)
                .withNestedProperties(List.of())
                .withRecordClass(false)
                .withConvertMethod("people")
                .build();
        lenient().when(classParamInfoFactory.create(any())).thenReturn(List.of(classParam));

        final var result = delegator.create(method);

        assertThat(result).hasSize(1);
        verify(classParamInfoFactory).create(any());
    }

    private ExecutableElement getMethod(final String methodName) {
        return ElementFilter.methodsIn(fixture.getEnclosedElements())
                .stream()
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .findFirst()
                .orElseThrow();
    }
}
