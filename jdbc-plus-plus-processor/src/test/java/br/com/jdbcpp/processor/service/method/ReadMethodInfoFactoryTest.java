package br.com.jdbcpp.processor.service.method;

import br.com.jdbcpp.api.Query;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.method.SelectCollectionMethodInfo;
import br.com.jdbcpp.processor.dto.method.SelectNullableMethodInfo;
import br.com.jdbcpp.processor.dto.method.SelectOptionalMethodInfo;
import br.com.jdbcpp.processor.dto.result.SelectReturnStrategy;
import br.com.jdbcpp.processor.service.statement.StatementInfoFactory;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.TypeUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static br.com.jdbcpp.api.ResultBuildStrategyType.CONSTRUCTOR;
import static br.com.jdbcpp.api.ResultBuildStrategyType.SETTER;
import static br.com.jdbcpp.api.ResultBuildStrategyType.SIMPLE_RESULT;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
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
    private CollectionUtil collectionUtil;
    @ProcessingEnv
    private ProcessingEnvironment processingEnv;
    @FixtureElement
    private TypeElement fixture;

    @ParameterizedTest
    @ValueSource(strings = {"findNameById", "findAgeById", "findIdByName", "findActiveById"})
    void shouldCreateSimpleResultMethod(final String methodName) throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var collection = processingEnv.getElementUtils().getTypeElement(Collection.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException, collection);

        final var method = getMethod(methodName);
        final var query = requireNonNull(method.getAnnotation(Query.class));

        final var builder = MethodInfo.builder()
                .withName(methodName)
                .withParams(List.of())
                .withClassPropertyMap(Map.of());

        when(typeUtil.isSimpleType(any(TypeMirror.class))).thenReturn(true);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(builder, method, query, nullReadException);

            assertThat(result.getStatement()).isEqualTo(statementInfo);
            assertThat(result.getPackException()).isEqualTo(sqlException);
            assertThat(result.getReturnType()).isEqualTo(method.getReturnType());
            assertThat(((SelectNullableMethodInfo)result).getStrategyType()).isEqualTo(SIMPLE_RESULT);
            assertThat(((SelectNullableMethodInfo)result).getContainerReturnTypeMirror()).isNull();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"findAllNames", "findAllAges"})
    void shouldCreateCollectionResultMethod(final String methodName) throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var collection = processingEnv.getElementUtils().getTypeElement(Collection.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException, collection);

        final var method = getMethod(methodName);
        final var query = requireNonNull(method.getAnnotation(Query.class));

        final var builder = MethodInfo.builder()
                .withName(methodName)
                .withParams(List.of())
                .withClassPropertyMap(Map.of());

        when(typeUtil.isSimpleType(any())).thenReturn(false);
        when(collectionUtil.isCollectionType(any())).thenReturn(true);
        when(collectionUtil.getCollectionElementType(any())).thenReturn(method.getReturnType());
        when(typeUtil.isNotSimpleType(any())).thenReturn(false);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(builder, method, query, nullReadException);

            assertThat(result.getStatement()).isEqualTo(statementInfo);
            assertThat(result.getPackException()).isEqualTo(sqlException);
            final var returnType = ((DeclaredType) method.getReturnType()).getTypeArguments().getFirst();
            assertThat(result.getReturnType()).isEqualTo(returnType);
            assertThat(((SelectCollectionMethodInfo)result).getStrategyType()).isEqualTo(SIMPLE_RESULT);
            assertThat(((SelectCollectionMethodInfo)result).getContainerReturnTypeMirror()).isEqualTo(method.getReturnType());
            assertThat(((SelectCollectionMethodInfo)result).getInstanceContainer()).isEqualTo(method.getReturnType());
        }
    }

    @ParameterizedTest
    @ValueSource( strings = {"findUsersByName", "findUsersByActive"})
    void shouldCreateObjectResultMethod(final String methodName) throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var collection = processingEnv.getElementUtils().getTypeElement(Collection.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException, collection);

        final var method = getMethod(methodName);
        final var query = requireNonNull(method.getAnnotation(Query.class));

        final var builder = MethodInfo.builder()
                .withName(methodName)
                .withParams(List.of())
                .withClassPropertyMap(Map.of());

        when(typeUtil.isSimpleType(any())).thenReturn(false);
        when(collectionUtil.isCollectionType(any())).thenReturn(false);
        when(typeUtil.isRecord(any())).thenReturn(false);

        final var strategies = List.<SelectReturnStrategy<?>>of();
        when(buildSetterStrategy.generateStrategyInfo(any())).thenReturn(strategies);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(builder, method, query, nullReadException);

            assertThat(result.getStatement()).isEqualTo(statementInfo);
            assertThat(result.getPackException()).isEqualTo(sqlException);
            final var returnType = ((DeclaredType) method.getReturnType()).getTypeArguments().getFirst();
            assertThat(result.getReturnType()).isEqualTo(returnType);
            assertThat(((SelectCollectionMethodInfo)result).getStrategyType()).isEqualTo(SETTER);
            assertThat(((SelectCollectionMethodInfo)result).getSetterStrategies()).isEqualTo(strategies);
            assertThat(((SelectCollectionMethodInfo)result).getContainerReturnTypeMirror()).isEqualTo(method.getReturnType());
        }
    }

    @Test
    void shouldCreateCollectionWithConstructorStrategy() throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var collection = processingEnv.getElementUtils().getTypeElement(Collection.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException, collection);

        final var ListType = processingEnv.getElementUtils().getTypeElement(List.class.getCanonicalName());
        final var user = processingEnv.getElementUtils().getTypeElement("com.example.ReadMethodInfoFactoryTest.User").asType();
        final var ListUser = processingEnv.getTypeUtils().getDeclaredType(ListType, user);

        final var method = getMethod("findUsersByNameWithConstructor");
        final var query = requireNonNull(method.getAnnotation(Query.class));

        final var builder = MethodInfo.builder()
                .withName("findUsersByNameWithConstructor")
                .withParams(List.of())
                .withClassPropertyMap(Map.of());

        final var returnType = ((DeclaredType) method.getReturnType()).getTypeArguments().getFirst();
        when(typeUtil.isSimpleType(any())).thenReturn(false);
        when(collectionUtil.isCollectionType(any())).thenReturn(true);
        when(collectionUtil.getCollectionElementType(any())).thenReturn(returnType);
        when(typeUtil.isNotSimpleType(any())).thenReturn(true);
        when(typeUtil.isRecord(any())).thenReturn(true);
        when(typeUtil.buildContainerTypeMirror(any(), any())).thenReturn(ListUser);

        final var strategies = List.<SelectReturnStrategy<?>>of();
        when(buildConstructorStrategy.generateStrategyInfo(any(), anyString())).thenReturn(strategies);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(builder, method, query, nullReadException);

            assertThat(result.getStatement()).isEqualTo(statementInfo);
            assertThat(result.getPackException()).isEqualTo(sqlException);
            assertThat(result.getReturnType()).isEqualTo(returnType);
            assertThat(((SelectCollectionMethodInfo)result).getStrategyType()).isEqualTo(CONSTRUCTOR);
            assertThat(((SelectCollectionMethodInfo)result).getConstructorStrategies()).isEqualTo(strategies);
            assertThat(((SelectCollectionMethodInfo)result).getContainerReturnTypeMirror()).isEqualTo(method.getReturnType());
            assertThat(processingEnv.getTypeUtils().isSameType(((SelectCollectionMethodInfo)result).getInstanceContainer(), method.getReturnType())).isTrue();
        }
    }

    @ParameterizedTest
    @CsvSource({
            "findUsersByNameWithCustomList, java.util.List, java.util.LinkedList",
            "findUsersByIdWithCustomSet, java.util.Set, java.util.LinkedHashSet"
    })
    void shouldCreateMethodWithCustomInstanceContainer(final String methodName,
                                                       final String collectionInterfaceTypeName,
                                                       final String collectionTypeName) throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var collection = processingEnv.getElementUtils().getTypeElement(Collection.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException, collection);

        final var collectionInterfaceType = processingEnv.getElementUtils().getTypeElement(collectionInterfaceTypeName);
        final var collectionType = processingEnv.getElementUtils().getTypeElement(collectionTypeName);
        final var user = processingEnv.getElementUtils().getTypeElement("com.example.ReadMethodInfoFactoryTest.User").asType();
        final var collectionInterfaceUser = processingEnv.getTypeUtils().getDeclaredType(collectionInterfaceType, user);
        final var collectionUser = processingEnv.getTypeUtils().getDeclaredType(collectionType, user);

        final var method = getMethod(methodName);
        final var query = requireNonNull(method.getAnnotation(Query.class));

        final var builder = MethodInfo.builder()
                .withName(methodName)
                .withParams(List.of())
                .withClassPropertyMap(Map.of());

        when(typeUtil.isSimpleType(any())).thenReturn(false);
        when(collectionUtil.isCollectionType(any())).thenReturn(true);
        when(collectionUtil.getCollectionElementType(any())).thenReturn(method.getReturnType());
        when(typeUtil.isNotSimpleType(any())).thenReturn(false);
        when(typeUtil.buildContainerTypeMirror(any(), any())).thenReturn(collectionUser);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(builder, method, query, nullReadException);

            assertThat(result.getStatement()).isEqualTo(statementInfo);
            assertThat(result.getPackException()).isEqualTo(sqlException);
            assertThat(result.getReturnType()).isEqualTo(user);
            assertThat(processingEnv.getTypeUtils().isSameType(((SelectCollectionMethodInfo)result).getContainerReturnTypeMirror(), collectionInterfaceUser)).isTrue();
            assertThat(((SelectCollectionMethodInfo)result).getInstanceContainer()).isEqualTo(collectionUser);
        }
    }

    @Test
    void shouldUseExplicitSetterStrategyWithCustomContainer() throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var collection = processingEnv.getElementUtils().getTypeElement(Collection.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException, collection);

        final var linkedListType = processingEnv.getElementUtils().getTypeElement(LinkedList.class.getCanonicalName());
        final var user = processingEnv.getElementUtils().getTypeElement("com.example.ReadMethodInfoFactoryTest.User").asType();
        final var linkedListUser = processingEnv.getTypeUtils().getDeclaredType(linkedListType, user);

        final var method = getMethod("findUsersByNameWithSetterAndCustomList");
        final var query = requireNonNull(method.getAnnotation(Query.class));

        final var builder = MethodInfo.builder()
                .withName("findUsersByNameWithSetterAndCustomList")
                .withParams(List.of())
                .withClassPropertyMap(Map.of());

        when(typeUtil.isSimpleType(any())).thenReturn(false);
        when(collectionUtil.isCollectionType(any())).thenReturn(true);
        when(collectionUtil.getCollectionElementType(any())).thenReturn(method.getReturnType());
        when(typeUtil.isNotSimpleType(any())).thenReturn(true);
        when(typeUtil.isRecord(any())).thenReturn(false);
        when(typeUtil.buildContainerTypeMirror(any(), any())).thenReturn(linkedListUser);

        final var returnType = ((DeclaredType) method.getReturnType()).getTypeArguments().getFirst();

        final var strategies = List.<SelectReturnStrategy<?>>of();
        when(buildSetterStrategy.generateStrategyInfo(any())).thenReturn(strategies);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(builder, method, query, nullReadException);

            assertThat(result.getStatement()).isEqualTo(statementInfo);
            assertThat(result.getPackException()).isEqualTo(sqlException);
            assertThat(result.getReturnType()).isEqualTo(returnType);
            assertThat(((SelectCollectionMethodInfo)result).getStrategyType()).isEqualTo(SETTER);
            assertThat(((SelectCollectionMethodInfo)result).getSetterStrategies()).isEqualTo(strategies);
            assertThat(((SelectCollectionMethodInfo)result).getContainerReturnTypeMirror()).isEqualTo(method.getReturnType());
            assertThat(((SelectCollectionMethodInfo)result).getInstanceContainer()).isEqualTo(linkedListUser);
        }
    }

    @Test
    void shouldUseExplicitConstructorStrategyWithCustomContainer() throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var collection = processingEnv.getElementUtils().getTypeElement(Collection.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException, collection);

        final var linkedListType = processingEnv.getElementUtils().getTypeElement(LinkedList.class.getCanonicalName());
        final var user = processingEnv.getElementUtils().getTypeElement("com.example.ReadMethodInfoFactoryTest.UserEntity").asType();
        final var linkedListUser = processingEnv.getTypeUtils().getDeclaredType(linkedListType, user);


        final var method = getMethod("findUsersByNameWithConstructorAndCustomList");
        final var query = requireNonNull(method.getAnnotation(Query.class));

        final var builder = MethodInfo.builder()
                .withName("findUsersByNameWithConstructorAndCustomList")
                .withParams(List.of())
                .withClassPropertyMap(Map.of());

        when(typeUtil.isSimpleType(any())).thenReturn(false);
        when(collectionUtil.isCollectionType(any())).thenReturn(true);
        when(collectionUtil.getCollectionElementType(any())).thenReturn(method.getReturnType());
        when(typeUtil.isNotSimpleType(any())).thenReturn(true);
        when(typeUtil.isRecord(any())).thenReturn(false);
        when(typeUtil.buildContainerTypeMirror(any(), any())).thenReturn(linkedListUser);

        final var returnType = ((DeclaredType) method.getReturnType()).getTypeArguments().getFirst();

        final var strategies = List.<SelectReturnStrategy<?>>of();
        when(buildConstructorStrategy.generateStrategyInfo(any(), anyString())).thenReturn(strategies);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(builder, method, query, nullReadException);

            assertThat(result.getStatement()).isEqualTo(statementInfo);
            assertThat(result.getPackException()).isEqualTo(sqlException);
            assertThat(result.getReturnType()).isEqualTo(returnType);
            assertThat(((SelectCollectionMethodInfo)result).getStrategyType()).isEqualTo(CONSTRUCTOR);
            assertThat(((SelectCollectionMethodInfo)result).getConstructorStrategies()).isEqualTo(strategies);
            assertThat(((SelectCollectionMethodInfo)result).getContainerReturnTypeMirror()).isEqualTo(method.getReturnType());
            assertThat(((SelectCollectionMethodInfo)result).getInstanceContainer()).isEqualTo(linkedListUser);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "findOptionalUserById, com.example.ReadMethodInfoFactoryTest.User",
            "findOptionalNameById, java.lang.String"
    })
    void shouldCreateOptionalResultMethod(final String methodName, final String returnTypeName) throws Exception{
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var collection = processingEnv.getElementUtils().getTypeElement(Collection.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException, collection);

        final var method = getMethod(methodName);
        final var query = requireNonNull(method.getAnnotation(Query.class));

        final var optionalType = processingEnv.getElementUtils().getTypeElement(Optional.class.getCanonicalName());
        final var returnType = processingEnv.getElementUtils().getTypeElement(returnTypeName).asType();
        final var optionalReturnType = processingEnv.getTypeUtils().getDeclaredType(optionalType, returnType);

        final var builder = MethodInfo.builder()
                .withName(methodName)
                .withParams(List.of())
                .withClassPropertyMap(Map.of());

        when(typeUtil.isSimpleType(any())).thenReturn(false);
        when(collectionUtil.isNotCollectionType(any())).thenReturn(true);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(builder, method, query, nullReadException);

            assertThat(result.getStatement()).isEqualTo(statementInfo);
            assertThat(result.getPackException()).isEqualTo(sqlException);
            assertThat(result.getReturnType()).isEqualTo(returnType);
            assertThat(((SelectOptionalMethodInfo)result).getStrategyType()).isEqualTo(SETTER);
            assertThat(processingEnv.getTypeUtils().isSameType(((SelectOptionalMethodInfo)result).getContainerReturnTypeMirror(), optionalReturnType)).isTrue();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"findUserByIdWithConstructor", "findUserById"})
    void shouldCreateRecordResultMethod(final String methodName) throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var collection = processingEnv.getElementUtils().getTypeElement(Collection.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException, collection);

        final var method = getMethod(methodName);
        final var query = requireNonNull(method.getAnnotation(Query.class));

        final var builder = MethodInfo.builder()
                .withName(methodName)
                .withParams(List.of())
                .withClassPropertyMap(Map.of());

        when(typeUtil.isSimpleType(any())).thenReturn(false);
        when(collectionUtil.isCollectionType(any())).thenReturn(false);
        when(typeUtil.isRecord(any())).thenReturn(true);

        final var strategies = List.<SelectReturnStrategy<?>>of();
        when(buildConstructorStrategy.generateStrategyInfo(any(), anyString())).thenReturn(strategies);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(builder, method, query, nullReadException);

            assertThat(result.getStatement()).isEqualTo(statementInfo);
            assertThat(result.getPackException()).isEqualTo(sqlException);
            assertThat(result.getReturnType()).isEqualTo(method.getReturnType());
            assertThat(((SelectNullableMethodInfo)result).getStrategyType()).isEqualTo(CONSTRUCTOR);
            assertThat(((SelectNullableMethodInfo)result).getConstructorStrategies()).isEqualTo(strategies);
            assertThat(((SelectNullableMethodInfo)result).getContainerReturnTypeMirror()).isNull();
        }
    }

    @Test
    void shouldThrowExceptionWhenValidateParamsFails() throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var collection = processingEnv.getElementUtils().getTypeElement(Collection.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullReadException, collection);

        final var method = getMethod("findNameById");
        final var query = requireNonNull(method.getAnnotation(Query.class));

        final var builder = MethodInfo.builder()
                .withName("findNameById")
                .withParams(List.of())
                .withClassPropertyMap(Map.of());

        when(typeUtil.isSimpleType(any())).thenReturn(true);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(builder, method, query, nullReadException);

            assertThat(result).isInstanceOf(SelectNullableMethodInfo.class);
        }
    }

    @Test
    void shouldUseCustomExceptionWhenProvided() throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var collection = processingEnv.getElementUtils().getTypeElement(Collection.class.getCanonicalName()).asType();
        final var nullReadException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var customException = processingEnv.getElementUtils().getTypeElement("com.example.ReadMethodInfoFactoryTest.CustomException").asType();
        final var factory = createFactory(sqlException, nullReadException, collection);

        final var method = getMethod("findNameById");
        final var query = requireNonNull(method.getAnnotation(Query.class));

        final var builder = MethodInfo.builder()
                .withName("findNameById")
                .withParams(List.of())
                .withClassPropertyMap(Map.of());

        when(typeUtil.isSimpleType(any())).thenReturn(true);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new br.com.jdbcpp.processor.dto.statement.StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(builder, method, query, customException);

            assertThat(result).isInstanceOf(SelectNullableMethodInfo.class);
        }
    }

    private ExecutableElement getMethod(final String methodName) {
        return ElementFilter.methodsIn(fixture.getEnclosedElements())
                .stream()
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .findFirst()
                .orElseThrow();
    }

    private ReadMethodInfoFactory createFactory(final TypeMirror sqlException,
                                                final TypeMirror nullReadException,
                                                final  TypeMirror collection) {
        return new ReadMethodInfoFactory(
                processingEnv.getTypeUtils(),
                buildConstructorStrategy,
                buildSetterStrategy,
                typeUtil,
                collectionUtil,
                nullReadException,
                sqlException,
                collection
        );
    }

}