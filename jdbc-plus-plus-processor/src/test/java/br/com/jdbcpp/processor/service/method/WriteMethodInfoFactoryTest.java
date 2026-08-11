package br.com.jdbcpp.processor.service.method;

import br.com.jdbcpp.api.Command;
import br.com.jdbcpp.processor.dto.method.DeleteMethod;
import br.com.jdbcpp.processor.dto.method.InsertMethod;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.method.UpdateMethod;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import br.com.jdbcpp.processor.exception.InvalidInputParamException;
import br.com.jdbcpp.processor.exception.InvalidMethodSignatureException;
import br.com.jdbcpp.processor.service.statement.StatementInfoFactory;
import br.com.jdbcpp.processor.service.validation.MethodValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class, MicroProcessorExtension.class})
@Fixture(
        resourcePath = "service/method/WriteMethodInfoFactoryTest.txt",
        packageName = "com.example"
)
class WriteMethodInfoFactoryTest {

    private record TestCase(
            String description,
            String methodName,
            Class<? extends MethodInfo> expectedType,
            boolean expectedReturnRowsAffected,
            boolean useCustomException
    ) {}

    @Mock
    private MethodValidator methodValidator;
    @ProcessingEnv
    private ProcessingEnvironment processingEnv;
    @FixtureElement
    private TypeElement fixture;

    private static Stream<Arguments> shouldCreateWriteMethod() {
        return Stream.of(
                Arguments.of(new TestCase("INSERT without rows affected", "insertUser", InsertMethod.class, false, false)),
                Arguments.of(new TestCase("INSERT with rows affected", "insertUserWithRowsAffected", InsertMethod.class, true, false)),
                Arguments.of(new TestCase("INSERT with custom exception", "insertUserWithCustomException", InsertMethod.class, false, true)),
                Arguments.of(new TestCase("UPDATE without rows affected", "updateUser", UpdateMethod.class, false, false)),
                Arguments.of(new TestCase("UPDATE with rows affected", "updateUserWithRowsAffected", UpdateMethod.class, true, false)),
                Arguments.of(new TestCase("UPDATE with custom exception", "updateUserWithCustomException", UpdateMethod.class, false, true)),
                Arguments.of(new TestCase("DELETE without rows affected", "deleteUser", DeleteMethod.class, false, false)),
                Arguments.of(new TestCase("DELETE with rows affected", "deleteUserWithRowsAffected", DeleteMethod.class, true, false)),
                Arguments.of(new TestCase("DELETE with custom exception", "deleteUserWithCustomException", DeleteMethod.class, false, true))
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldCreateWriteMethod(final TestCase testCase) throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var nullWriteException = processingEnv.getElementUtils().getTypeElement(Command.None.class.getCanonicalName()).asType();
        final var customException = processingEnv.getElementUtils().getTypeElement("com.example.WriteMethodInfoFactoryTest.CustomException").asType();
        final var factory = createFactory(sqlException, nullWriteException);

        final var method = getMethod(testCase.methodName());
        final var command = method.getAnnotation(Command.class);
        final var params = List.<ParamInfo>of();
        final var classPropertyMap = Map.<String, List<ParamInfo>>of();
        final var packException = testCase.useCustomException() ? customException : nullWriteException;

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(method, params, classPropertyMap, command, packException);

            assertThat(result).isInstanceOf(testCase.expectedType());
            assertThat(result.getName()).isEqualTo(testCase.methodName());

            switch (result) {
                case InsertMethod insertMethod -> assertThat(insertMethod.isReturnRowsAffected()).isEqualTo(testCase.expectedReturnRowsAffected());
                case UpdateMethod updateMethod -> assertThat(updateMethod.isReturnRowsAffected()).isEqualTo(testCase.expectedReturnRowsAffected());
                case DeleteMethod deleteMethod -> assertThat(deleteMethod.isReturnRowsAffected()).isEqualTo(testCase.expectedReturnRowsAffected());
                default -> throw new AssertionError("Unexpected result type: " + result.getClass());
            }

            verify(methodValidator).validateParams(method, params, classPropertyMap, result.getStatement().params());
            verify(methodValidator).validateExceptionThrow(method, testCase.useCustomException() ? customException : sqlException);
        }
    }

    @Test
    void shouldThrowExceptionWhenValidateParamsFails() throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var nullWriteException = processingEnv.getElementUtils().getTypeElement(Command.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullWriteException);

        final var method = getMethod("insertUser");
        final var command = method.getAnnotation(Command.class);
        final var params = List.<ParamInfo>of();
        final var classPropertyMap = Map.<String, List<ParamInfo>>of();

        doThrow(new InvalidInputParamException("Invalid params", method))
                .when(methodValidator)
                .validateParams(method, params, classPropertyMap, List.of());

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            assertThatThrownBy(() -> factory.create(method, params, classPropertyMap, command, nullWriteException))
                    .isInstanceOf(InvalidInputParamException.class)
                    .hasMessage("Invalid params");
        }
        verify(methodValidator, never()).validateExceptionThrow(any(ExecutableElement.class), any(TypeMirror.class));
    }

    @Test
    void shouldThrowExceptionWhenValidateExceptionThrowFails() throws Throwable {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var nullWriteException = processingEnv.getElementUtils().getTypeElement(Command.None.class.getCanonicalName()).asType();
        final var factory = createFactory(sqlException, nullWriteException);

        final var method = getMethod("insertUser");
        final var command = method.getAnnotation(Command.class);
        final var params = List.<ParamInfo>of();
        final var classPropertyMap = Map.<String, List<ParamInfo>>of();

        doThrow(new InvalidMethodSignatureException("Invalid exception", method))
                .when(methodValidator)
                .validateExceptionThrow(method, sqlException);

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            assertThatThrownBy(() -> factory.create(method, params, classPropertyMap, command, nullWriteException))
                    .isInstanceOf(InvalidMethodSignatureException.class)
                    .hasMessage("Invalid exception");
        }
    }

    private ExecutableElement getMethod(final String methodName) {
        return ElementFilter.methodsIn(fixture.getEnclosedElements())
                .stream()
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .findFirst()
                .orElseThrow();
    }

    private WriteMethodInfoFactory createFactory(final TypeMirror sqlException, final TypeMirror nullWriteException) {
        return new WriteMethodInfoFactory(methodValidator, sqlException, nullWriteException, processingEnv.getTypeUtils());
    }

}