package br.com.jdbcpp.processor.service.validation;

import br.com.jdbcpp.processor.exception.InvalidMethodSignatureException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import util.MicroProcessor;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MethodValidatorTest {

    private static Stream<Arguments> shouldValidateReturnWhenRowsAffectedTrue() {
        return Stream.of(
                Arguments.of("methodReturningInt", "INSERT"),
                Arguments.of("methodReturningInteger", "INSERT"),
                Arguments.of("methodReturningLongWrapper", "INSERT"),
                Arguments.of("methodReturningLong", "INSERT"),

                Arguments.of("methodReturningInt", "UPDATE"),
                Arguments.of("methodReturningInteger", "UPDATE"),
                Arguments.of("methodReturningLongWrapper", "UPDATE"),
                Arguments.of("methodReturningLong", "UPDATE"),

                Arguments.of("methodReturningInt", "DELETE"),
                Arguments.of("methodReturningInteger", "DELETE"),
                Arguments.of("methodReturningLongWrapper", "DELETE"),
                Arguments.of("methodReturningLong", "DELETE")
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldValidateReturnWhenRowsAffectedTrue(final String methodName, final String operation) {
        final var microProcessor = new MicroProcessor<>(
                "service/validation/MethodValidatorTest.txt",
                "com.example",
                processingEnv -> {
                    final var longType = processingEnv.getElementUtils().getTypeElement("java.lang.Long").asType();
                    final var integerType = processingEnv.getElementUtils().getTypeElement("java.lang.Integer").asType();
                    final var throwable = processingEnv.getElementUtils().getTypeElement("java.lang.Throwable").asType();
                    final var sqlException = processingEnv.getElementUtils().getTypeElement("java.sql.SQLException").asType();
                    return new MethodValidator(
                            processingEnv.getTypeUtils(),
                            longType,
                            integerType,
                            throwable,
                            sqlException
                    );
                }
        );
        microProcessor.compile((testInstance, fixture) -> {
            final var method = ElementFilter.methodsIn(fixture.getEnclosedElements())
                    .stream()
                    .filter(m -> m.getSimpleName().toString().equals(methodName))
                    .findFirst()
                    .orElseThrow();

            assertThatNoException().isThrownBy(() ->
                    testInstance.validateReturn(method, true, operation, List.of()));
        });
    }

    private static Stream<Arguments> shouldThrowExceptionWhenRowsAffectedTrueAndInvalidReturn() {
        return Stream.of(
                Arguments.of("methodReturningString", "INSERT"),
                Arguments.of("methodReturningObject", "UPDATE"),
                Arguments.of("methodReturningVoid", "DELETE")
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldThrowExceptionWhenRowsAffectedTrueAndInvalidReturn(final String methodName, final String operation) {
        final var microProcessor = new MicroProcessor<>(
                "service/validation/MethodValidatorTest.txt",
                "com.example",
                processingEnv -> {
                    final var longType = processingEnv.getElementUtils().getTypeElement("java.lang.Long").asType();
                    final var integerType = processingEnv.getElementUtils().getTypeElement("java.lang.Integer").asType();
                    final var throwable = processingEnv.getElementUtils().getTypeElement("java.lang.Throwable").asType();
                    final var sqlException = processingEnv.getElementUtils().getTypeElement("java.sql.SQLException").asType();
                    return new MethodValidator(
                            processingEnv.getTypeUtils(),
                            longType,
                            integerType,
                            throwable,
                            sqlException
                    );
                }
        );
        microProcessor.compile((testInstance, fixture) -> {
            final var method = ElementFilter.methodsIn(fixture.getEnclosedElements())
                    .stream()
                    .filter(m -> m.getSimpleName().toString().equals(methodName))
                    .findFirst()
                    .orElseThrow();

            assertThatThrownBy(() -> testInstance.validateReturn(method, true, operation, List.of()))
                    .isInstanceOf(InvalidMethodSignatureException.class)
                    .hasMessageContaining("is defined to return rows affected, but return is not int or long");
        });
    }

    private static Stream<Arguments> shouldValidateReturnWhenRowsAffectedFalse() {
        return Stream.of(
                Arguments.of("methodReturningVoid", "INSERT"),
                Arguments.of("methodReturningString", "INSERT"),
                Arguments.of("methodReturningVoid", "UPDATE"),
                Arguments.of("methodReturningString", "UPDATE"),
                Arguments.of("methodReturningVoid", "DELETE")
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldValidateReturnWhenRowsAffectedFalse(final String methodName, final String operation) {
        final var microProcessor = new MicroProcessor<>(
                "service/validation/MethodValidatorTest.txt",
                "com.example",
                processingEnv -> {
                    final var longType = processingEnv.getElementUtils().getTypeElement("java.lang.Long").asType();
                    final var integerType = processingEnv.getElementUtils().getTypeElement("java.lang.Integer").asType();
                    final var throwable = processingEnv.getElementUtils().getTypeElement("java.lang.Throwable").asType();
                    final var sqlException = processingEnv.getElementUtils().getTypeElement("java.sql.SQLException").asType();
                    return new MethodValidator(
                            processingEnv.getTypeUtils(),
                            longType,
                            integerType,
                            throwable,
                            sqlException
                    );
                }
        );
        microProcessor.compile((testInstance, fixture) -> {
            final var method = ElementFilter.methodsIn(fixture.getEnclosedElements())
                    .stream()
                    .filter(m -> m.getSimpleName().toString().equals(methodName))
                    .findFirst()
                    .orElseThrow();

            assertThatNoException().isThrownBy(() ->
                    testInstance.validateReturn(method, false, operation, List.of(method.getReturnType()))
            );
        });
    }

    private static Stream<Arguments> shouldThrowExceptionWhenRowsAffectedFalseAndInvalidReturn() {
        return Stream.of(
                Arguments.of("methodReturningInt", List.of()),
                Arguments.of("methodReturningString", List.of())
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldThrowExceptionWhenRowsAffectedFalseAndInvalidReturn(final String methodName, final List<TypeMirror> validReturns) {
        final var microProcessor = new MicroProcessor<>(
                "service/validation/MethodValidatorTest.txt",
                "com.example",
                processingEnv -> {
                    final var longType = processingEnv.getElementUtils().getTypeElement("java.lang.Long").asType();
                    final var integerType = processingEnv.getElementUtils().getTypeElement("java.lang.Integer").asType();
                    final var throwable = processingEnv.getElementUtils().getTypeElement("java.lang.Throwable").asType();
                    final var sqlException = processingEnv.getElementUtils().getTypeElement("java.sql.SQLException").asType();
                    return new MethodValidator(
                            processingEnv.getTypeUtils(),
                            longType,
                            integerType,
                            throwable,
                            sqlException
                    );
                }
        );
        microProcessor.compile((testInstance, fixture) -> {
            final var method = ElementFilter.methodsIn(fixture.getEnclosedElements())
                    .stream()
                    .filter(m -> m.getSimpleName().toString().equals(methodName))
                    .findFirst()
                    .orElseThrow();

            assertThatThrownBy(() -> testInstance.validateReturn(method, false, "INSERT", validReturns))
                    .isInstanceOf(InvalidMethodSignatureException.class)
                    .hasMessageContaining("without rowsAffected result has invalid config");
        });
    }

}