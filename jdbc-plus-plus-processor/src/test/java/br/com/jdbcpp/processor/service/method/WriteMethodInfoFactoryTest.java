package br.com.jdbcpp.processor.service.method;

import br.com.jdbcpp.api.Command;
import br.com.jdbcpp.processor.dto.method.DeleteMethod;
import br.com.jdbcpp.processor.dto.method.InsertMethod;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.method.UpdateMethod;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import br.com.jdbcpp.processor.service.statement.StatementInfoFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

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
    void shouldCreateWriteMethod(final TestCase testCase) {
        final var sqlException = processingEnv.getElementUtils().getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var nullWriteException = processingEnv.getElementUtils().getTypeElement(Command.None.class.getCanonicalName()).asType();
        final var customException = processingEnv.getElementUtils().getTypeElement("com.example.WriteMethodInfoFactoryTest.CustomException").asType();
        final var factory = createFactory(sqlException, nullWriteException);

        final var method = getMethod(testCase.methodName());
        final var command = requireNonNull(method.getAnnotation(Command.class));
        final var packException = testCase.useCustomException() ? customException : nullWriteException;

        final var builder = MethodInfo.builder()
                .withName(testCase.methodName())
                .withReturnType(method.getReturnType())
                .withParams(List.of())
                .withClassPropertyMap(Map.of());

        try (MockedStatic<StatementInfoFactory> mockedFactory = mockStatic(StatementInfoFactory.class)) {
            final var statementInfo = new StatementInfo(List.of("test"), List.of());
            mockedFactory.when(() -> StatementInfoFactory.create(anyString())).thenReturn(statementInfo);

            final var result = factory.create(builder, command, packException);

            final var expectedPackException = testCase.useCustomException() ? customException : sqlException;
            assertThat(result.getPackException()).isEqualTo(expectedPackException);
            assertThat(result.getStatement()).isEqualTo(statementInfo);

            switch (result) {
                case InsertMethod insertMethod -> assertThat(insertMethod.isReturnRowsAffected()).isEqualTo(testCase.expectedReturnRowsAffected());
                case UpdateMethod updateMethod -> assertThat(updateMethod.isReturnRowsAffected()).isEqualTo(testCase.expectedReturnRowsAffected());
                case DeleteMethod deleteMethod -> assertThat(deleteMethod.isReturnRowsAffected()).isEqualTo(testCase.expectedReturnRowsAffected());
                default -> throw new AssertionError("Unexpected result type: " + result.getClass());
            }
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
        return new WriteMethodInfoFactory(sqlException, nullWriteException, processingEnv.getTypeUtils());
    }

}