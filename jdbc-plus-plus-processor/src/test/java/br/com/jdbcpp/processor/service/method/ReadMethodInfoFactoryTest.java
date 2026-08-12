package br.com.jdbcpp.processor.service.method;

import br.com.jdbcpp.api.Query;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.result.SelectReturnStrategy;
import br.com.jdbcpp.processor.exception.InvalidInputParamException;
import br.com.jdbcpp.processor.exception.InvalidMethodSignatureException;
import br.com.jdbcpp.processor.service.statement.StatementInfoFactory;
import br.com.jdbcpp.processor.service.validation.MethodValidator;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.TypeUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import util.extension.Fixture;
import util.extension.FixtureElement;
import util.extension.MicroProcessorExtension;
import util.extension.ProcessingEnv;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static br.com.jdbcpp.api.ResultBuildStrategyType.CONSTRUCTOR;
import static br.com.jdbcpp.api.ResultBuildStrategyType.SETTER;
import static br.com.jdbcpp.api.ResultBuildStrategyType.SIMPLE_RESULT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, MicroProcessorExtension.class})
@Fixture(
        resourcePath = "service/method/ReadMethodInfoFactoryTest.txt",
        packageName = "com.example"
)
class ReadMethodInfoFactoryTest {

    @Mock
    private BuildConstructorStrategy buildConstructorStrategy;
    @Mock
    private BuildSetterStrategy buildSetterStrategy;
    @Mock
    private TypeUtil typeUtil;
    @Mock
    private MethodValidator methodValidator;
    @Mock
    private CollectionUtil collectionUtil;
    @ProcessingEnv
    private ProcessingEnvironment processingEnv;
    @FixtureElement
    private TypeElement fixture;

    @ParameterizedTest
    @ValueSource(strings = {"findNameById", "findAgeById", "findIdByName", "findActiveById"})
    void shouldCreateSimpleResultMethod(final String methodName) throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException);

        final var method = getMethod(methodName);
        final var query = method.getAnnotation(Query.class);
        final var params = List.<ParamInfo>of();
        final var classPropertyMap = Map.<String, List<ParamInfo>>of();

        when(typeUtil.isSimpleType(any())).thenReturn(true);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(method, params, classPropertyMap, query, nullReadException);

            assertThat(result).isInstanceOf(MethodInfo.class);
            assertThat(result.getName()).isEqualTo(methodName);
            assertThat(result.getReturnType()).isEqualTo(method.getReturnType());
            assertThat(((SelectMethodInfo)result).getStrategyType()).isEqualTo(SIMPLE_RESULT);
            assertThat(((SelectMethodInfo)result).getContainerReturnTypeMirror()).isNull();
            assertThat(((SelectMethodInfo)result).getInstanceContainer()).isNull();

            verify(methodValidator).validateParams(method, params, classPropertyMap, result.getStatement().params());
            verify(methodValidator).validateExceptionThrow(method, sqlException);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"findAllNames", "findAllAges"})
    void shouldCreateCollectionResultMethod(final String methodName) throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException);

        final var method = getMethod(methodName);
        final var query = method.getAnnotation(Query.class);
        final var params = List.<ParamInfo>of();
        final var classPropertyMap = Map.<String, List<ParamInfo>>of();

        when(typeUtil.isSimpleType(any())).thenReturn(false);
        when(collectionUtil.isCollectionType(any())).thenReturn(true);
        when(collectionUtil.getCollectionElementType(any())).thenReturn(method.getReturnType());
        when(typeUtil.isNotSimpleType(any())).thenReturn(false);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(method, params, classPropertyMap, query, nullReadException);

            assertThat(result).isInstanceOf(SelectMethodInfo.class);
            assertThat(result.getName()).isEqualTo(methodName);
            final var returnType = ((DeclaredType) method.getReturnType()).getTypeArguments().getFirst();
            assertThat(result.getReturnType()).isEqualTo(returnType);
            assertThat(((SelectMethodInfo)result).getStrategyType()).isEqualTo(SIMPLE_RESULT);
            assertThat(((SelectMethodInfo)result).getContainerReturnTypeMirror()).isEqualTo(method.getReturnType());
            assertThat(((SelectMethodInfo)result).getInstanceContainer()).isEqualTo(method.getReturnType());

            verify(methodValidator).validateParams(method, params, classPropertyMap, result.getStatement().params());
            verify(methodValidator).validateExceptionThrow(method, sqlException);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "findUsersByName", "findUsersByActive"})
    void shouldCreateObjectResultMethod(final String methodName) throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException);

        final var method = getMethod(methodName);
        final var query = method.getAnnotation(Query.class);
        final var params = List.<ParamInfo>of();
        final var classPropertyMap = Map.<String, List<ParamInfo>>of();

        when(typeUtil.isSimpleType(any())).thenReturn(false);
        when(collectionUtil.isCollectionType(any())).thenReturn(false);
        when(typeUtil.isRecord(any())).thenReturn(false);

        final var strategies = List.<SelectReturnStrategy<?>>of();
        when(buildSetterStrategy.generateStrategyInfo(any())).thenReturn(strategies);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(method, params, classPropertyMap, query, nullReadException);

            assertThat(result).isInstanceOf(SelectMethodInfo.class);
            assertThat(result.getName()).isEqualTo(methodName);
            final var returnType = ((DeclaredType) method.getReturnType()).getTypeArguments().getFirst();
            assertThat(result.getReturnType()).isEqualTo(returnType);
            assertThat(((SelectMethodInfo)result).getStrategyType()).isEqualTo(SETTER);
            assertThat(((SelectMethodInfo)result).getContainerReturnTypeMirror()).isEqualTo(method.getReturnType());
            assertThat(((SelectMethodInfo)result).getInstanceContainer()).isEqualTo(method.getReturnType());

            verify(methodValidator).validateParams(method, params, classPropertyMap, result.getStatement().params());
            verify(methodValidator).validateExceptionThrow(method, sqlException);
        }
    }

    @Test
    void shouldCreateCollectionWithConstructorStrategy() throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException);

        final var method = getMethod("findUsersByNameWithConstructor");
        final var query = method.getAnnotation(Query.class);
        final var params = List.<ParamInfo>of();
        final var classPropertyMap = Map.<String, List<ParamInfo>>of();

        final var returnType = ((DeclaredType) method.getReturnType()).getTypeArguments().getFirst();
        when(typeUtil.isSimpleType(any())).thenReturn(false);
        when(collectionUtil.isCollectionType(any())).thenReturn(true);
        when(collectionUtil.getCollectionElementType(any())).thenReturn(returnType);
        when(typeUtil.isNotSimpleType(any())).thenReturn(true);
        when(typeUtil.isRecord(any())).thenReturn(true);

        final var strategies = List.<SelectReturnStrategy<?>>of();
        when(buildConstructorStrategy.generateStrategyInfo(any(), anyString())).thenReturn(strategies);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(method, params, classPropertyMap, query, nullReadException);

            assertThat(result).isInstanceOf(SelectMethodInfo.class);
            assertThat(result.getName()).isEqualTo("findUsersByNameWithConstructor");
            assertThat(result.getReturnType()).isEqualTo(returnType);
            assertThat(((SelectMethodInfo)result).getStrategyType()).isEqualTo(CONSTRUCTOR);
            assertThat(((SelectMethodInfo)result).getContainerReturnTypeMirror()).isEqualTo(method.getReturnType());
            assertThat(((SelectMethodInfo)result).getInstanceContainer()).isEqualTo(method.getReturnType());

            verify(methodValidator).validateParams(method, params, classPropertyMap, result.getStatement().params());
            verify(methodValidator).validateExceptionThrow(method, sqlException);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"findUsersByNameWithCustomList", "findUsersByIdWithCustomSet"})
    void shouldCreateMethodWithCustomInstanceContainer(final String methodName) throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException);

        final var method = getMethod(methodName);
        final var query = method.getAnnotation(Query.class);
        final var params = List.<ParamInfo>of();
        final var classPropertyMap = Map.<String, List<ParamInfo>>of();

        when(typeUtil.isSimpleType(any())).thenReturn(false);
        when(collectionUtil.isCollectionType(any())).thenReturn(true);
        when(collectionUtil.getCollectionElementType(any())).thenReturn(method.getReturnType());
        when(typeUtil.isNotSimpleType(any())).thenReturn(false);
        when(typeUtil.isNotList(any())).thenReturn(true);
        final var returnType = ((DeclaredType) method.getReturnType()).getTypeArguments().getFirst();
        final var containerTypeMirror = mock(TypeMirror.class);
        when(typeUtil.buildContainerTypeMirror(any(), eq(returnType))).thenReturn(containerTypeMirror);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(method, params, classPropertyMap, query, nullReadException);

            assertThat(result).isInstanceOf(SelectMethodInfo.class);
            assertThat(result.getName()).isEqualTo(methodName);

            assertThat(result.getReturnType()).isEqualTo(returnType);
            assertThat(((SelectMethodInfo)result).getContainerReturnTypeMirror()).isEqualTo(method.getReturnType());
            assertThat(((SelectMethodInfo)result).getInstanceContainer()).isEqualTo(containerTypeMirror);

            verify(methodValidator).validateParams(method, params, classPropertyMap, result.getStatement().params());
            verify(methodValidator).validateExceptionThrow(method, sqlException);
        }
    }

    @Test
    void shouldUseExplicitSetterStrategyWithCustomContainer() throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException);

        final var method = getMethod("findUsersByNameWithSetterAndCustomList");
        final var query = method.getAnnotation(Query.class);
        final var params = List.<ParamInfo>of();
        final var classPropertyMap = Map.<String, List<ParamInfo>>of();

        when(typeUtil.isSimpleType(any())).thenReturn(false);
        when(collectionUtil.isCollectionType(any())).thenReturn(true);
        when(collectionUtil.getCollectionElementType(any())).thenReturn(method.getReturnType());
        when(typeUtil.isNotSimpleType(any())).thenReturn(true);
        when(typeUtil.isRecord(any())).thenReturn(false);

        final var returnType = ((DeclaredType) method.getReturnType()).getTypeArguments().getFirst();

        final var strategies = List.<SelectReturnStrategy<?>>of();
        when(buildSetterStrategy.generateStrategyInfo(any())).thenReturn(strategies);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(method, params, classPropertyMap, query, nullReadException);

            assertThat(result).isInstanceOf(SelectMethodInfo.class);
            assertThat(result.getName()).isEqualTo("findUsersByNameWithSetterAndCustomList");
            assertThat(result.getReturnType()).isEqualTo(returnType);
            assertThat(((SelectMethodInfo)result).getStrategyType()).isEqualTo(SETTER);
            assertThat(((SelectMethodInfo)result).getContainerReturnTypeMirror()).isEqualTo(method.getReturnType());
            assertThat(((SelectMethodInfo)result).getInstanceContainer()).isEqualTo(method.getReturnType());

            verify(methodValidator).validateParams(method, params, classPropertyMap, result.getStatement().params());
            verify(methodValidator).validateExceptionThrow(method, sqlException);
        }
    }

    @Test
    void shouldUseExplicitConstructorStrategyWithCustomContainer() throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException);

        final var method = getMethod("findUsersByNameWithConstructorAndCustomList");
        final var query = method.getAnnotation(Query.class);
        final var params = List.<ParamInfo>of();
        final var classPropertyMap = Map.<String, List<ParamInfo>>of();

        when(typeUtil.isSimpleType(any())).thenReturn(false);
        when(collectionUtil.isCollectionType(any())).thenReturn(true);
        when(collectionUtil.getCollectionElementType(any())).thenReturn(method.getReturnType());
        when(typeUtil.isNotSimpleType(any())).thenReturn(true);
        when(typeUtil.isRecord(any())).thenReturn(false);

        final var returnType = ((DeclaredType) method.getReturnType()).getTypeArguments().getFirst();

        final var strategies = List.<SelectReturnStrategy<?>>of();
        when(buildConstructorStrategy.generateStrategyInfo(any(), anyString())).thenReturn(strategies);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(method, params, classPropertyMap, query, nullReadException);

            assertThat(result).isInstanceOf(SelectMethodInfo.class);
            assertThat(result.getName()).isEqualTo("findUsersByNameWithConstructorAndCustomList");
            assertThat(result.getReturnType()).isEqualTo(returnType);
            assertThat(((SelectMethodInfo)result).getStrategyType()).isEqualTo(CONSTRUCTOR);
            assertThat(((SelectMethodInfo)result).getContainerReturnTypeMirror()).isEqualTo(method.getReturnType());
            assertThat(((SelectMethodInfo)result).getInstanceContainer()).isEqualTo(method.getReturnType());

            verify(methodValidator).validateParams(method, params, classPropertyMap, result.getStatement().params());
            verify(methodValidator).validateExceptionThrow(method, sqlException);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"findUserByIdWithConstructor", "findUserById"})
    void shouldCreateRecordResultMethod(final String methodName) throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException);

        final var method = getMethod(methodName);
        final var query = method.getAnnotation(Query.class);
        final var params = List.<ParamInfo>of();
        final var classPropertyMap = Map.<String, List<ParamInfo>>of();

        when(typeUtil.isSimpleType(any())).thenReturn(false);
        when(collectionUtil.isCollectionType(any())).thenReturn(false);
        when(typeUtil.isRecord(any())).thenReturn(true);

        final var strategies = List.<SelectReturnStrategy<?>>of();
        when(buildConstructorStrategy.generateStrategyInfo(any(), anyString())).thenReturn(strategies);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(method, params, classPropertyMap, query, nullReadException);

            assertThat(result).isInstanceOf(SelectMethodInfo.class);
            assertThat(result.getName()).isEqualTo(methodName);
            assertThat(result.getReturnType()).isEqualTo(method.getReturnType());
            assertThat(((SelectMethodInfo)result).getStrategyType()).isEqualTo(CONSTRUCTOR);
            assertThat(((SelectMethodInfo)result).getContainerReturnTypeMirror()).isNull();
            assertThat(((SelectMethodInfo)result).getInstanceContainer()).isNull();

            verify(methodValidator).validateParams(method, params, classPropertyMap, result.getStatement().params());
            verify(methodValidator).validateExceptionThrow(method, sqlException);
        }
    }

    @Test
    void shouldThrowExceptionWhenMethodReturnsVoid() {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException);

        final var method = getMethod("invalidVoidReturn");
        final var query = method.getAnnotation(Query.class);
        final var params = List.<ParamInfo>of();
        final var classPropertyMap = Map.<String, List<ParamInfo>>of();

        assertThatThrownBy(() -> factory.create(method, params, classPropertyMap, query, nullReadException))
                .isInstanceOf(InvalidMethodSignatureException.class)
                .hasMessageContaining("invalidVoidReturn")
                .hasMessageContaining("returns void");
    }

    @Test
    void shouldThrowExceptionWhenValidateParamsFails() throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException);

        final var method = getMethod("findNameById");
        final var query = method.getAnnotation(Query.class);
        final var params = List.<ParamInfo>of();
        final var classPropertyMap = Map.<String, List<ParamInfo>>of();

        when(typeUtil.isSimpleType(any())).thenReturn(true);
        doThrow(new InvalidInputParamException("Invalid params", method))
                .when(methodValidator)
                .validateParams(method, params, classPropertyMap, List.of());

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            assertThatThrownBy(() -> factory.create(method, params, classPropertyMap, query, nullReadException))
                    .isInstanceOf(InvalidInputParamException.class)
                    .hasMessage("Invalid params");
        }
        verify(methodValidator, never()).validateExceptionThrow(any(ExecutableElement.class), any(TypeMirror.class));
    }

    @Test
    void shouldThrowExceptionWhenValidateExceptionThrowFails() throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException);

        final var method = getMethod("findNameById");
        final var query = method.getAnnotation(Query.class);
        final var params = List.<ParamInfo>of();
        final var classPropertyMap = Map.<String, List<ParamInfo>>of();

        when(typeUtil.isSimpleType(any())).thenReturn(true);
        doThrow(new InvalidMethodSignatureException("Invalid exception", method))
                .when(methodValidator)
                .validateExceptionThrow(method, sqlException);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            assertThatThrownBy(() -> factory.create(method, params, classPropertyMap, query, nullReadException))
                    .isInstanceOf(InvalidMethodSignatureException.class)
                    .hasMessage("Invalid exception");
        }
    }

    @Test
    void shouldUseCustomExceptionWhenProvided() throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var customException = processingEnv.getElementUtils().getTypeElement("com.example.ReadMethodInfoFactoryTest.CustomException").asType();
        final var factory = createFactory(sqlException, nullReadException);

        final var method = getMethod("findNameById");
        final var query = method.getAnnotation(Query.class);
        final var params = List.<ParamInfo>of();
        final var classPropertyMap = Map.<String, List<ParamInfo>>of();

        when(typeUtil.isSimpleType(any())).thenReturn(true);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(method, params, classPropertyMap, query, customException);

            verify(methodValidator).validateExceptionThrow(method, customException);
        }
    }

    private ExecutableElement getMethod(final String methodName) {
        return ElementFilter.methodsIn(fixture.getEnclosedElements())
                .stream()
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .findFirst()
                .orElseThrow();
    }

    private ReadMethodInfoFactory createFactory(final TypeMirror sqlException, final TypeMirror nullReadException) {
        return new ReadMethodInfoFactory(
                processingEnv.getTypeUtils(),
                buildConstructorStrategy,
                buildSetterStrategy,
                typeUtil,
                methodValidator,
                collectionUtil,
                nullReadException,
                sqlException
        );
    }

}