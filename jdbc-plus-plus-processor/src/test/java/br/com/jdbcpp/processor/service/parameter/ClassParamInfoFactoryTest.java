package br.com.jdbcpp.processor.service.parameter;

import br.com.jdbcpp.processor.dto.parameter.ClassParamInfo;
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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith({MockitoExtension.class, MicroProcessorExtension.class})
@Fixture(
        resourcePath = "service/parameter/ClassParamInfoFactoryTest.txt",
        packageName = "com.example"
)
class ClassParamInfoFactoryTest {

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

    private ClassParamInfoFactory createFactory() {
        return new ClassParamInfoFactory(
                processingEnv.getTypeUtils(),
                processingEnv.getElementUtils(),
                arrayUtil,
                collectionUtil,
                typeUtil
        );
    }

    @Test
    void shouldCreateSimpleClassParam() throws Exception {
        final var factory = createFactory();
        final var method = getMethod("simpleClassParam");
        final var param = method.getParameters().getFirst();
        
        final var result = factory.create(param);
        
        assertThat(result).hasSize(1);
        final var classParam = (ClassParamInfo) result.getFirst();
        assertThat(classParam.getName()).isEqualTo("person");
        assertThat(classParam.isRecordClass()).isFalse();
        assertThat(classParam.getNestedProperties()).hasSize(2);
        
        final var nameProperty = (SimpleParamInfo) classParam.getNestedProperties().getFirst();
        assertThat(nameProperty.getName()).isEqualTo("name");
        assertThat(nameProperty.getConvertMethod()).isEqualTo("getName");
        
        final var ageProperty = (SimpleParamInfo) classParam.getNestedProperties().get(1);
        assertThat(ageProperty.getName()).isEqualTo("age");
        assertThat(ageProperty.getConvertMethod()).isEqualTo("getAge");
    }

    @Test
    void shouldCreateClassWithCollectionParam() throws Exception {
        final var factory = createFactory();
        final var method = getMethod("classWithCollectionParam");
        final var param = method.getParameters().getFirst();
        final var addressType = processingEnv.getElementUtils()
                .getTypeElement("com.example.ClassParamInfoFactoryTest.Address").asType();
        final var stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();

        final var listType = processingEnv.getElementUtils().getTypeElement(List.class.getCanonicalName());

        final var addressList = processingEnv.getTypeUtils().getDeclaredType(listType, addressType);

        lenient().when(collectionUtil.getCollectionElementType(any())).thenAnswer(i -> {
            final var arg = i.getArgument(0, TypeMirror.class);
            if (processingEnv.getTypeUtils().isSameType(arg, addressList)){
                return  addressType;
            } else {
                return null;
            }
        });
        lenient().when(typeUtil.isSimpleType(addressType)).thenReturn(false);
        lenient().when(typeUtil.isSimpleType(stringType)).thenReturn(true);

        final var result = factory.create(param);

        assertThat(result).hasSize(1);
        final var classParam = (ClassParamInfo) result.getFirst();
        assertThat(classParam.getName()).isEqualTo("addresses");
        assertThat(classParam.getContainerType()).isEqualTo(addressType);
        assertThat(classParam.getNestedProperties()).hasSize(2);
    }

    @Test
    void shouldCreateClassWithArrayParam() throws Exception {
        final var factory = createFactory();
        final var method = getMethod("classWithArrayParam");
        final var param = method.getParameters().getFirst();
        final var phoneType = processingEnv.getElementUtils()
                .getTypeElement("com.example.ClassParamInfoFactoryTest.Phone").asType();
        final var stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();

        final var phoneArray = processingEnv.getTypeUtils().getArrayType(phoneType);

        lenient().when(arrayUtil.getArrayElementType(any())).thenAnswer(i -> {
            final var arg = i.getArgument(0, TypeMirror.class);
            if (processingEnv.getTypeUtils().isSameType(arg, phoneArray)){
                return  phoneType;
            } else {
                return null;
            }
        });
        lenient().when(typeUtil.isSimpleType(phoneType)).thenReturn(false);
        lenient().when(typeUtil.isSimpleType(stringType)).thenReturn(true);

        final var result = factory.create(param);

        assertThat(result).hasSize(1);
        final var classParam = (ClassParamInfo) result.getFirst();
        assertThat(classParam.getName()).isEqualTo("phones");
        assertThat(classParam.getContainerType()).isEqualTo(phoneType);
        assertThat(classParam.getNestedProperties()).hasSize(1);
    }

    @Test
    void shouldCreateRecordParam() throws Exception {
        final var factory = createFactory();
        final var method = getMethod("recordParam");
        final var param = method.getParameters().getFirst();
        final var recordType = processingEnv.getElementUtils()
                .getTypeElement("com.example.ClassParamInfoFactoryTest.UserRecord").asType();
        
        lenient().when(typeUtil.isRecord(recordType)).thenReturn(true);
        
        final var result = factory.create(param);
        
        assertThat(result).hasSize(1);
        final var classParam = (ClassParamInfo) result.getFirst();
        assertThat(classParam.getName()).isEqualTo("userRecord");
        assertThat(classParam.isRecordClass()).isTrue();
        assertThat(classParam.getNestedProperties()).hasSize(2);
    }

    @Test
    void shouldHandleAnnotatedFields() throws Exception {
        final var factory = createFactory();
        final var method = getMethod("classWithAnnotatedField");
        final var param = method.getParameters().getFirst();

        final var result = factory.create(param);

        assertThat(result).hasSize(1);
        final var classParam = (ClassParamInfo) result.getFirst();
        assertThat(classParam.getNestedProperties()).hasSize(2);

        final var nameProperty = (SimpleParamInfo) classParam.getNestedProperties().getFirst();
        assertThat(nameProperty.getName()).isEqualTo("name");
        assertThat(nameProperty.getConvertMethod()).isEqualTo("getFullName");
        assertThat(nameProperty.getQueryParamName()).isEqualTo("full_name");

        final var secretProperty = (SimpleParamInfo) classParam.getNestedProperties().get(1);
        assertThat(secretProperty.getName()).isEqualTo("secret");
        assertThat(secretProperty.isIgnore()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenEnumMethodNotFound() {
        final var factory = createFactory();
        final var method = getMethod("classWithInvalidEnumMethod");
        final var param = method.getParameters().getFirst();
        final var statusType = processingEnv.getElementUtils()
                .getTypeElement("com.example.ClassParamInfoFactoryTest.Status").asType();

        lenient().when(typeUtil.isEnum(statusType)).thenReturn(true);

        assertThatThrownBy(() -> factory.create(param))
                .isInstanceOf(InvalidInputParamException.class)
                .hasMessageContaining("does not contains method nonExistentMethod");
    }

    @Test
    void shouldCreateClassWithComplexCollection() throws Exception {
        final var factory = createFactory();
        final var method = getMethod("classWithComplexCollection");
        final var param = method.getParameters().getFirst();
        final var addressType = processingEnv.getElementUtils()
                .getTypeElement("com.example.ClassParamInfoFactoryTest.Address").asType();
        final var stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();

        final var listType = processingEnv.getElementUtils().getTypeElement(List.class.getCanonicalName());
        final var addressList = processingEnv.getTypeUtils().getDeclaredType(listType, addressType);

        lenient().when(collectionUtil.getCollectionElementType(any())).thenAnswer(i -> {
            final var arg = i.getArgument(0, TypeMirror.class);
            if (processingEnv.getTypeUtils().isSameType(arg, addressList)){
                return  addressType;
            } else {
                return null;
            }
        });
        lenient().when(typeUtil.isSimpleType(addressType)).thenReturn(false);
        lenient().when(typeUtil.isSimpleType(stringType)).thenReturn(true);

        final var result = factory.create(param);

        assertThat(result).hasSize(1);
        final var classParam = (ClassParamInfo) result.getFirst();
        assertThat(classParam.getName()).isEqualTo("addresses");
        assertThat(classParam.getContainerType()).isEqualTo(addressType);
        assertThat(classParam.getNestedProperties()).hasSize(2);
    }

    @Test
    void shouldExtractOnlyPrivateNonStaticFields() throws Exception {
        final var factory = createFactory();
        final var method = getMethod("classWithMixedFields");
        final var param = method.getParameters().getFirst();

        final var result = factory.create(param);

        assertThat(result).hasSize(1);
        final var classParam = (ClassParamInfo) result.getFirst();
        assertThat(classParam.getNestedProperties()).hasSize(1);

        final var field = (SimpleParamInfo) classParam.getNestedProperties().getFirst();
        assertThat(field.getName()).isEqualTo("privateField");
    }

    @Test
    void shouldFindRecordMethod() throws Exception {
        final var factory = createFactory();
        final var recordType = processingEnv.getElementUtils()
                .getTypeElement("com.example.ClassParamInfoFactoryTest.UserRecord").asType();
        final var stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
        
        final var method = factory.findMethod(recordType, "username", stringType);
        
        assertThat(method).isEqualTo("username");
    }

    @Test
    void shouldFindJavaBeanGetterMethod() throws Exception {
        final var factory = createFactory();
        final var personType = processingEnv.getElementUtils()
                .getTypeElement("com.example.ClassParamInfoFactoryTest.Person").asType();
        final var stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
        
        final var method = factory.findMethod(personType, "name", stringType);
        
        assertThat(method).isEqualTo("getName");
    }

    @Test
    void shouldThrowExceptionWhenMethodNotFound() {
        final var factory = createFactory();
        final var personType = processingEnv.getElementUtils()
                .getTypeElement("com.example.ClassParamInfoFactoryTest.Person").asType();
        final var stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
        
        assertThatThrownBy(() -> factory.findMethod(personType, "nonExistent", stringType))
                .isInstanceOf(InvalidInputParamException.class)
                .hasMessageContaining("has none valid public method to access property nonExistent");
    }

    private ExecutableElement getMethod(final String methodName) {
        return ElementFilter.methodsIn(fixture.getEnclosedElements())
                .stream()
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .findFirst()
                .orElseThrow();
    }
}
