package br.com.jdbcpp.processor.service.parameter;

import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfo;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith({MockitoExtension.class, MicroProcessorExtension.class})
@Fixture(
        resourcePath = "service/parameter/SimpleParamInfoFactoryTest.txt",
        packageName = "com.example"
)
class SimpleParamInfoFactoryTest {

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

    private SimpleParamInfoFactory createFactory() {
        return new SimpleParamInfoFactory(
                processingEnv.getTypeUtils(),
                processingEnv.getElementUtils(),
                arrayUtil,
                collectionUtil,
                typeUtil
        );
    }

    @Test
    void shouldCreateSimpleParamWithoutAnnotation() throws Exception {
        final var factory = createFactory();
        final var method = getMethod("simpleParam");
        
        final var result = factory.create(method);
        
        assertThat(result).hasSize(1);
        final var param = (SimpleParamInfo) result.getFirst();
        assertThat(param.getName()).isEqualTo("name");
        assertThat(param.getQueryParamName()).isEqualTo("name");
        assertThat(param.getConvertMethod()).isEqualTo("name");
        assertThat(param.isIgnore()).isFalse();
    }

    @Test
    void shouldCreateSimpleParamWithCustomAnnotation() throws Exception {
        final var factory = createFactory();
        final var method = getMethod("simpleParamWithAnnotation");
        
        final var result = factory.create(method);
        
        assertThat(result).hasSize(1);
        final var param = (SimpleParamInfo) result.getFirst();
        assertThat(param.getName()).isEqualTo("name");
        assertThat(param.getQueryParamName()).isEqualTo("custom_field");
        assertThat(param.getConvertMethod()).isEqualTo("customName");
        assertThat(param.isIgnore()).isFalse();
    }

    @Test
    void shouldCreateCollectionParam() throws Exception {
        final var factory = createFactory();
        final var method = getMethod("collectionParam");
        final var stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
        
        lenient().when(collectionUtil.getCollectionElementType(any())).thenReturn(stringType);
        
        final var result = factory.create(method);
        
        assertThat(result).hasSize(1);
        final var param = (SimpleParamInfo) result.getFirst();
        assertThat(param.getName()).isEqualTo("names");
        assertThat(param.getQueryParamName()).isEqualTo("names");
        assertThat(param.getContainerType()).isEqualTo(stringType);
    }

    @Test
    void shouldCreateArrayParam() throws Exception {
        final var factory = createFactory();
        final var method = getMethod("arrayParam");
        final var stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
        
        lenient().when(arrayUtil.getArrayElementType(any())).thenReturn(stringType);
        
        final var result = factory.create(method);
        
        assertThat(result).hasSize(1);
        final var param = (SimpleParamInfo) result.getFirst();
        assertThat(param.getName()).isEqualTo("names");
        assertThat(param.getQueryParamName()).isEqualTo("names");
        assertThat(param.getContainerType()).isEqualTo(stringType);
    }

    @Test
    void shouldCreateEnumParamWithCustomMethod() throws Exception {
        final var factory = createFactory();
        final var method = getMethod("enumParamWithAnnotation");
        final var statusType = processingEnv.getElementUtils().getTypeElement("com.example.SimpleParamInfoFactoryTest.Status").asType();
        
        lenient().when(typeUtil.isEnum(statusType)).thenReturn(true);
        
        final var result = factory.create(method);
        
        assertThat(result).hasSize(1);
        final var param = (SimpleParamInfo) result.getFirst();
        assertThat(param.getName()).isEqualTo("status");
        assertThat(param.isCustomEnum()).isTrue();
        assertThat(param.getConvertMethod()).isEqualTo("status.getValue()");
    }

    @Test
    void shouldCreateMultipleParams() throws Exception {
        final var factory = createFactory();
        final var method = getMethod("multipleParams");
        final var stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
        
        lenient().when(collectionUtil.getCollectionElementType(any())).thenReturn(stringType);
        
        final var result = factory.create(method);
        
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getName()).isEqualTo("name");
        assertThat(result.get(1).getName()).isEqualTo("age");
        assertThat(result.get(2).getName()).isEqualTo("tags");
    }

    @Test
    void shouldCreateIgnoredParam() throws Exception {
        final var factory = createFactory();
        final var method = getMethod("ignoredParam");
        
        final var result = factory.create(method);
        
        assertThat(result).hasSize(1);
        final var param = (SimpleParamInfo) result.getFirst();
        assertThat(param.getName()).isEqualTo("name");
        assertThat(param.isIgnore()).isTrue();
    }

    @Test
    void shouldUseCamelToSnakeCaseForQueryParamName() throws Exception {
        final var factory = createFactory();
        final var method = getMethod("simpleParam");
        
        final var result = factory.create(method);
        
        final var param = (SimpleParamInfo) result.getFirst();
        assertThat(param.getQueryParamName()).isEqualTo("name");
    }

    private ExecutableElement getMethod(final String methodName) {
        return ElementFilter.methodsIn(fixture.getEnclosedElements())
                .stream()
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .findFirst()
                .orElseThrow();
    }
}