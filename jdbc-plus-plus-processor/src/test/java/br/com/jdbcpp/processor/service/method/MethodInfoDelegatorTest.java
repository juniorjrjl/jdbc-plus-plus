package br.com.jdbcpp.processor.service.method;

import br.com.jdbcpp.api.Command;
import br.com.jdbcpp.api.CommandType;
import br.com.jdbcpp.api.Query;
import br.com.jdbcpp.api.ResultBuildStrategyType;
import br.com.jdbcpp.processor.dto.method.DeleteMethod;
import br.com.jdbcpp.processor.dto.method.InsertMethod;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.method.SelectNullableMethodInfo;
import br.com.jdbcpp.processor.dto.method.UpdateMethod;
import br.com.jdbcpp.processor.dto.parameter.ClassParamInfo;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import br.com.jdbcpp.processor.exception.InvalidMethodSignatureException;
import br.com.jdbcpp.processor.service.parameter.ParamPathExtractor;
import br.com.jdbcpp.processor.service.parameter.ParameterInfoDelegator;
import br.com.jdbcpp.processor.service.validation.MethodValidator;
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
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static br.com.jdbcpp.processor.dto.method.QueryType.NULLABLE;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, MicroProcessorExtension.class})
@Fixture(
        resourcePath = "service/method/MethodInfoDelegatorTest.txt",
        packageName = "com.example"
)
class MethodInfoDelegatorTest {

    @Mock
    private ParameterInfoDelegator parameterInfoDelegator;
    @Mock
    private ParamPathExtractor paramPathExtractor;
    @Mock
    private WriteMethodInfoFactory writeMethodInfoFactory;
    @Mock
    private MethodValidator methodValidator;
    @Mock
    private ReadMethodInfoFactory readMethodInfoFactory;
    @Mock
    private TypeUtil typeUtil;

    @ProcessingEnv
    private ProcessingEnvironment processingEnv;
    @FixtureElement
    private TypeElement fixture;

    @Test
    void shouldBuildInsertMethod() throws Throwable {
        final var method = getMethod("insertUser");
        final var command = requireNonNull(method.getAnnotation(Command.class));
        final var delegator = createDelegator();

        final var params = List.<ParamInfo>of();
        final var classPropertyMap = Map.<String, List<ParamInfo>>of();
        final var statementInfo = new StatementInfo(List.of("INSERT INTO user"), List.of());
        final var packException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var methodInfo = MethodInfo.builder()
                .withName("insertUser")
                .withReturnType(method.getReturnType())
                .withParams(params)
                .withClassPropertyMap(classPropertyMap)
                .withStatement(statementInfo)
                .withPackException(packException)
                .<InsertMethod.InsertMethodBuilder>asWriteType(CommandType.INSERT)
                .withReturnRowsAffected(false)
                .build();

        when(parameterInfoDelegator.create(method)).thenReturn(params);
        lenient().when(typeUtil.getTypeMirrorFromClass(any())).thenReturn(packException);
        when(writeMethodInfoFactory.create(any(), eq(command), any())).thenReturn(methodInfo);

        final var result = delegator.build(method);

        assertThat(result).isEqualTo(methodInfo);

        final var builderCaptor = forClass(MethodInfo.MethodInfoBuilder.class);
        verify(writeMethodInfoFactory).create(builderCaptor.capture(), eq(command), eq(packException));
        assertBuilderFields(builderCaptor.getValue(), "insertUser", params, classPropertyMap);

        verify(methodValidator).validateWriteReturn(method, classPropertyMap, command.returnRowsAffected(), "INSERT");
        verify(methodValidator).validateParams(method, params, classPropertyMap, statementInfo.params());
        verify(methodValidator).validateExceptionThrow(method, packException);
    }

    @Test
    void shouldBuildUpdateMethod() throws Throwable {
        final var method = getMethod("updateUser");
        final var command = requireNonNull(method.getAnnotation(Command.class));
        final var delegator = createDelegator();

        final var params = List.<ParamInfo>of();
        final var classPropertyMap = Map.<String, List<ParamInfo>>of();
        final var statementInfo = new StatementInfo(List.of("UPDATE user"), List.of());
        final var packException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var methodInfo = MethodInfo.builder()
                .withName("updateUser")
                .withReturnType(method.getReturnType())
                .withParams(params)
                .withClassPropertyMap(classPropertyMap)
                .withStatement(statementInfo)
                .withPackException(packException)
                .<UpdateMethod.UpdateMethodBuilder>asWriteType(CommandType.UPDATE)
                .withReturnRowsAffected(true)
                .build();

        when(parameterInfoDelegator.create(method)).thenReturn(params);
        lenient().when(typeUtil.getTypeMirrorFromClass(any())).thenReturn(packException);
        when(writeMethodInfoFactory.create(any(), eq(command), any())).thenReturn(methodInfo);

        final var result = delegator.build(method);

        assertThat(result).isEqualTo(methodInfo);

        final var builderCaptor = forClass(MethodInfo.MethodInfoBuilder.class);
        verify(writeMethodInfoFactory).create(builderCaptor.capture(), eq(command), eq(packException));
        assertBuilderFields(builderCaptor.getValue(), "updateUser", params, classPropertyMap);

        verify(methodValidator).validateWriteReturn(method, classPropertyMap, command.returnRowsAffected(), "UPDATE");
        verify(methodValidator).validateParams(method, params, classPropertyMap, statementInfo.params());
        verify(methodValidator).validateExceptionThrow(method, packException);
    }

    @Test
    void shouldBuildDeleteMethod() throws Throwable {
        final var method = getMethod("deleteUser");
        final var command = requireNonNull(method.getAnnotation(Command.class));
        final var delegator = createDelegator();

        final var params = List.<ParamInfo>of();
        final var classPropertyMap = Map.<String, List<ParamInfo>>of();
        final var statementInfo = new StatementInfo(List.of("DELETE FROM user"), List.of());
        final var packException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var methodInfo = MethodInfo.builder()
                .withName("deleteUser")
                .withReturnType(method.getReturnType())
                .withParams(params)
                .withClassPropertyMap(classPropertyMap)
                .withStatement(statementInfo)
                .withPackException(packException)
                .<DeleteMethod.DeleteMethodBuilder>asWriteType(CommandType.DELETE)
                .withReturnRowsAffected(false)
                .build();

        when(parameterInfoDelegator.create(method)).thenReturn(params);
        lenient().when(typeUtil.getTypeMirrorFromClass(any())).thenReturn(packException);
        when(writeMethodInfoFactory.create(any(), eq(command), any())).thenReturn(methodInfo);

        final var result = delegator.build(method);

        assertThat(result).isEqualTo(methodInfo);

        final var builderCaptor = forClass(MethodInfo.MethodInfoBuilder.class);
        verify(writeMethodInfoFactory).create(builderCaptor.capture(), eq(command), eq(packException));
        assertBuilderFields(builderCaptor.getValue(), "deleteUser", params, classPropertyMap);

        verify(methodValidator).validateWriteReturn(method, classPropertyMap, command.returnRowsAffected(), "DELETE");
        verify(methodValidator).validateParams(method, params, classPropertyMap, statementInfo.params());
        verify(methodValidator).validateExceptionThrow(method, packException);
    }

    @Test
    void shouldBuildSelectMethod() throws Throwable {
        final var method = getMethod("findUserById");
        final var query = requireNonNull(method.getAnnotation(Query.class));
        final var delegator = createDelegator();

        final var params = List.<ParamInfo>of();
        final var classPropertyMap = Map.<String, List<ParamInfo>>of();
        final var statementInfo = new StatementInfo(List.of("SELECT * FROM user"), List.of());
        final var packException = processingEnv.getElementUtils().getTypeElement(Query.None.class.getCanonicalName()).asType();
        final var methodInfo = MethodInfo.builder()
                .withName("findUserById")
                .withReturnType(method.getReturnType())
                .withParams(params)
                .withClassPropertyMap(classPropertyMap)
                .withStatement(statementInfo)
                .withPackException(packException)
                .<SelectNullableMethodInfo.SelectNullableMethodInfoBuilder>asReadType(NULLABLE)
                .withStrategyType(ResultBuildStrategyType.CONSTRUCTOR)
                .build();

        when(parameterInfoDelegator.create(method)).thenReturn(params);
        lenient().when(typeUtil.getTypeMirrorFromClass(any())).thenReturn(packException);
        when(readMethodInfoFactory.create(any(), eq(method), eq(query), any())).thenReturn(methodInfo);

        final var result = delegator.build(method);

        assertThat(result).isEqualTo(methodInfo);

        final var builderCaptor = forClass(MethodInfo.MethodInfoBuilder.class);
        verify(readMethodInfoFactory).create(builderCaptor.capture(), eq(method), eq(query), eq(packException));
        assertBuilderFields(builderCaptor.getValue(), "findUserById", params, classPropertyMap);

        verify(methodValidator).validateReadReturn(method);
        verify(methodValidator).validateParams(method, params, classPropertyMap, statementInfo.params());
        verify(methodValidator).validateExceptionThrow(method, packException);
    }

    @Test
    void shouldBuildMethodWithClassParam() throws Throwable {
        final var method = getMethod("insertUserWithClassParam");
        final var command = requireNonNull(method.getAnnotation(Command.class));
        final var delegator = createDelegator();

        final var classParamInfo = ClassParamInfo.builder()
                .withName("user")
                .withType(processingEnv.getElementUtils().getTypeElement("com.example.MethodInfoDelegatorTest.User").asType())
                .withNestedProperties(List.of())
                .withRecordClass(false)
                .withConvertMethod("")
                .build();
        final var params = List.<ParamInfo>of(classParamInfo);
        final var classPropertyMap = Map.of("user", List.<ParamInfo>of());
        final var statementInfo = new StatementInfo(List.of("INSERT INTO user"), List.of());
        final var packException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var methodInfo = MethodInfo.builder()
                .withName("insertUserWithClassParam")
                .withReturnType(method.getReturnType())
                .withParams(params)
                .withClassPropertyMap(classPropertyMap)
                .withStatement(statementInfo)
                .withPackException(packException)
                .<InsertMethod.InsertMethodBuilder>asWriteType(CommandType.INSERT)
                .withReturnRowsAffected(false)
                .build();

        when(parameterInfoDelegator.create(method)).thenReturn(params);
        when(paramPathExtractor.build(classParamInfo)).thenReturn(classPropertyMap);
        lenient().when(typeUtil.getTypeMirrorFromClass(any())).thenReturn(packException);
        when(writeMethodInfoFactory.create(any(), eq(command), any())).thenReturn(methodInfo);

        final var result = delegator.build(method);

        assertThat(result).isEqualTo(methodInfo);

        final var builderCaptor = forClass(MethodInfo.MethodInfoBuilder.class);
        verify(writeMethodInfoFactory).create(builderCaptor.capture(), eq(command), eq(packException));
        assertBuilderFields(builderCaptor.getValue(), "insertUserWithClassParam", params, classPropertyMap);

        verify(paramPathExtractor).build(classParamInfo);
        verify(methodValidator).validateWriteReturn(method, classPropertyMap, false, "INSERT");
        verify(methodValidator).validateParams(method, params, classPropertyMap, statementInfo.params());
        verify(methodValidator).validateExceptionThrow(method, packException);
    }

    @Test
    void shouldThrowExceptionWhenMethodHasNoAnnotation() throws Throwable {
        final var method = getMethod("methodWithoutAnnotation");
        final var delegator = createDelegator();

        final var params = List.<ParamInfo>of();
        when(parameterInfoDelegator.create(method)).thenReturn(params);

        assertThatThrownBy(() -> delegator.build(method))
                .isInstanceOf(InvalidMethodSignatureException.class)
                .hasMessageContaining("methodWithoutAnnotation");
    }

    private ExecutableElement getMethod(final String methodName) {
        return ElementFilter.methodsIn(fixture.getEnclosedElements())
                .stream()
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .findFirst()
                .orElseThrow();
    }

    private MethodInfoDelegator createDelegator() {
        return new MethodInfoDelegator(
                parameterInfoDelegator,
                paramPathExtractor,
                writeMethodInfoFactory,
                methodValidator,
                readMethodInfoFactory,
                typeUtil
        );
    }

    private void assertBuilderFields(final MethodInfo.MethodInfoBuilder builder,
                                     final String expectedName,
                                     final List<ParamInfo> expectedParams,
                                     final Map<String, List<ParamInfo>> expectedClassPropertyMap) throws Exception {
        final var nameField = MethodInfo.MethodInfoBuilder.class.getDeclaredField("name");
        nameField.setAccessible(true);
        assertThat(nameField.get(builder)).isEqualTo(expectedName);
        final var paramsField = MethodInfo.MethodInfoBuilder.class.getDeclaredField("params");
        paramsField.setAccessible(true);
        assertThat(paramsField.get(builder)).isEqualTo(expectedParams);
        final var classPropertyMapField = MethodInfo.MethodInfoBuilder.class.getDeclaredField("classPropertyMap");
        classPropertyMapField.setAccessible(true);
        assertThat(classPropertyMapField.get(builder)).isEqualTo(expectedClassPropertyMap);
    }
}
