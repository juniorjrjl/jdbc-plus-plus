package br.com.jdbcpp.processor.service.validation;

import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementParam;
import br.com.jdbcpp.processor.exception.InvalidInputParamException;
import br.com.jdbcpp.processor.exception.InvalidMethodSignatureException;
import br.com.jdbcpp.processor.exception.MoreParamsThanStatementNeedException;
import br.com.jdbcpp.processor.util.TypeUtil;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import util.extension.Fixture;
import util.extension.FixtureElement;
import util.extension.MicroProcessorExtension;
import util.extension.ProcessingEnv;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MicroProcessorExtension.class)
@Fixture(
        resourcePath = "service/validation/MethodValidatorTest.txt",
        packageName = "com.example"
)
class MethodValidatorTest {

    @Mock
    private TypeUtil typeUtil;
    @ProcessingEnv
    private ProcessingEnvironment processingEnv;
    @FixtureElement
    private TypeElement fixture;
    private MethodValidator createMethodValidator(){
        final var longType = processingEnv.getElementUtils()
                .getTypeElement("java.lang.Long").asType();
        final var integerType = processingEnv.getElementUtils()
                .getTypeElement("java.lang.Integer").asType();
        final var throwable = processingEnv.getElementUtils()
                .getTypeElement("java.lang.Throwable").asType();
        final var sqlException = processingEnv.getElementUtils()
                .getTypeElement("java.sql.SQLException").asType();
        return new MethodValidator(
                processingEnv.getTypeUtils(),
                longType,
                integerType,
                throwable,
                sqlException
        );
    }


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
        final var methodValidator = createMethodValidator();
        final var method = ElementFilter.methodsIn(fixture.getEnclosedElements())
                .stream()
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .findFirst()
                .orElseThrow();

        assertThatNoException().isThrownBy(() ->
                methodValidator.validateWriteReturn(method, Collections.emptyMap(), true, operation));
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
        final var methodValidator = createMethodValidator();
        final var method = ElementFilter.methodsIn(fixture.getEnclosedElements())
                .stream()
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .findFirst()
                .orElseThrow();

        assertThatThrownBy(() -> methodValidator.validateWriteReturn(method, Collections.emptyMap(), true, operation))
                .isInstanceOf(InvalidMethodSignatureException.class)
                .hasMessageContaining("is defined to return rows affected, but return is not int or long");
    }

    private static Stream<Arguments> shouldValidateReturnWhenRowsAffectedFalse() {
        return Stream.of(
                Arguments.of("methodReturningVoid", "INSERT", Collections.emptyMap()),
                Arguments.of("methodReturningStringWithClassParam", "INSERT", Map.of("param", List.of())),
                Arguments.of("methodReturningVoid", "UPDATE", Collections.emptyMap()),
                Arguments.of("methodReturningStringWithClassParam", "UPDATE", Map.of("param", List.of())),
                Arguments.of("methodReturningVoid", "DELETE", Collections.emptyMap())
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldValidateReturnWhenRowsAffectedFalse(final String methodName, final String operation, final Map<String, List<ParamInfo>> classPropertyMap) {
        final var methodValidator = createMethodValidator();
        final var method = ElementFilter.methodsIn(fixture.getEnclosedElements())
                .stream()
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .findFirst()
                .orElseThrow();

        assertThatNoException().isThrownBy(() ->
                methodValidator.validateWriteReturn(method, classPropertyMap, false, operation)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"methodReturningInt", "methodReturningLong", "methodReturningInteger", "methodReturningLongWrapper"})
    void shouldThrowExceptionWhenRowsAffectedFalseAndInvalidReturn(final String methodName) {
        final var methodValidator = createMethodValidator();
        final var method = ElementFilter.methodsIn(fixture.getEnclosedElements())
                .stream()
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .findFirst()
                .orElseThrow();

        assertThatThrownBy(() -> methodValidator.validateWriteReturn(method, Collections.emptyMap(), false, "INSERT"))
                .isInstanceOf(InvalidMethodSignatureException.class)
                .hasMessageContaining("without rowsAffected result has invalid config");
    }

    @ParameterizedTest
    @ValueSource(strings = {"methodThrowingSQLException", "methodThrowingCustomExceptionWithThrowableConstructor"})
    void shouldValidateExceptionThrow(final String methodName) {
        final var methodValidator = createMethodValidator();
        final var method = ElementFilter.methodsIn(fixture.getEnclosedElements())
                .stream()
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .findFirst()
                .orElseThrow();
        final var exception = method.getThrownTypes().getFirst();

        assertThatNoException().isThrownBy(() ->
                methodValidator.validateExceptionThrow(method, exception));
    }

    @ParameterizedTest
    @ValueSource(strings = {"methodThrowingCustomExceptionWithoutThrowableConstructor", "methodThrowingCustomExceptionWithTwoArguments"})
    void shouldThrowExceptionWhenCustomExceptionWithoutThrowableConstructor(final String methodName) {
        final var methodValidator = createMethodValidator();
        final var method = ElementFilter.methodsIn(fixture.getEnclosedElements())
                .stream()
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .findFirst()
                .orElseThrow();
        final var exception = method.getThrownTypes().getFirst();

        assertThatThrownBy(() -> methodValidator.validateExceptionThrow(method, exception))
                .isInstanceOf(InvalidMethodSignatureException.class)
                .hasMessageContaining("create a constructor with a Throwable parameter");
    }

    private static Stream<Arguments> shouldValidateParamsWhenMatching() {
        final Function<ProcessingEnvironment, List<ParamInfo>> listCallback = env -> List.of(
                SimpleParamInfo.builder()
                        .withName("name")
                        .withType(env.getElementUtils().getTypeElement("java.lang.String").asType())
                        .withCustomEnum(true)
                        .withQueryParamName("name")
                        .withConvertMethod("getName")
                        .build(),
                SimpleParamInfo.builder()
                        .withName("age")
                        .withType(env.getElementUtils().getTypeElement("java.lang.Integer").asType())
                        .withCustomEnum(true)
                        .withQueryParamName("age")
                        .withConvertMethod("getAge")
                        .build(),
                SimpleParamInfo.builder()
                        .withName("userEmail")
                        .withType(env.getElementUtils().getTypeElement("java.lang.String").asType())
                        .withCustomEnum(true)
                        .withQueryParamName("email")
                        .withConvertMethod("getEmail")
                        .build()
        );
        return Stream.of(
                Arguments.of(
                        listCallback,
                        Collections.emptyMap()
                ),
                Arguments.of(
                        listCallback,
                        Map.of(
                                "name", List.of(),
                                "age", List.of(),
                                "email", List.of()
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldValidateParamsWhenMatching(final Function<ProcessingEnvironment, List<ParamInfo>> paramsCallback,
                                          final Map<String, List<ParamInfo>> classPropertyMap) {
        final var methodValidator = createMethodValidator();
        final var method = ElementFilter.methodsIn(fixture.getEnclosedElements())
                .stream()
                .filter(m -> m.getSimpleName().toString().equals("methodWithMatchingParams"))
                .findFirst()
                .orElseThrow();

        final List<ParamInfo> params = paramsCallback.apply(processingEnv);

        final var statementParams = List.of(
                StatementParam.simple("name"),
                StatementParam.simple("age"),
                StatementParam.simple("email")
        );

        assertThatNoException().isThrownBy(() ->
                methodValidator.validateParams(method, params, classPropertyMap, statementParams));
    }


    private static Stream<Arguments> shouldThrowExceptionWhenExtraMethodParams(){
        final Function<ProcessingEnvironment, List<ParamInfo>> listCallback = env -> List.of(
                SimpleParamInfo.builder()
                        .withName("name")
                        .withType(env.getElementUtils().getTypeElement("java.lang.String").asType())
                        .withCustomEnum(true)
                        .withQueryParamName("name")
                        .withConvertMethod("getName")
                        .build(),
                SimpleParamInfo.builder()
                        .withName("age")
                        .withType(env.getElementUtils().getTypeElement("java.lang.Integer").asType())
                        .withCustomEnum(true)
                        .withQueryParamName("age")
                        .withConvertMethod("getAge")
                        .build(),
                SimpleParamInfo.builder()
                        .withName("userEmail")
                        .withType(env.getElementUtils().getTypeElement("java.lang.Integer").asType())
                        .withCustomEnum(true)
                        .withQueryParamName("email")
                        .withConvertMethod("getEmail")
                        .build()
        );
        return Stream.of(
                Arguments.of(
                        listCallback,
                        Collections.emptyMap()
                ),
                Arguments.of(
                        listCallback,
                        Map.of(
                                "name", Collections.emptyList(),
                                "age", Collections.emptyList(),
                                "email", Collections.emptyList()
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldThrowExceptionWhenExtraMethodParams(final Function<ProcessingEnvironment, List<ParamInfo>> paramsCallback,
                                                   final Map<String, List<ParamInfo>> classPropertyMap) {
        final var methodValidator = createMethodValidator();
        final var method = ElementFilter.methodsIn(fixture.getEnclosedElements())
                .stream()
                .filter(m -> m.getSimpleName().toString().equals("methodWithExtraMethodParams"))
                .findFirst()
                .orElseThrow();

        final List<ParamInfo> params = paramsCallback.apply(processingEnv);

        final var statementParams = List.of(
                StatementParam.simple("name"),
                StatementParam.simple("age")
        );

        assertThatThrownBy(() -> methodValidator.validateParams(method, params, classPropertyMap, statementParams))
                .isInstanceOf(MoreParamsThanStatementNeedException.class)
                .hasMessageContaining("received a follow ignored params");
    }

    private static Stream<Arguments> shouldThrowExceptionWhenExtraStatementParams(){
        final Function<ProcessingEnvironment, List<ParamInfo>> listCallback = env -> List.of(
                SimpleParamInfo.builder()
                        .withName("name")
                        .withType(env.getElementUtils().getTypeElement("java.lang.String").asType())
                        .withCustomEnum(true)
                        .withQueryParamName("name")
                        .withConvertMethod("getName")
                        .build(),
                SimpleParamInfo.builder()
                        .withName("userAge")
                        .withType(env.getElementUtils().getTypeElement("java.lang.String").asType())
                        .withCustomEnum(true)
                        .withQueryParamName("age")
                        .withConvertMethod("getAge")
                        .build()
        );
        return Stream.of(
                Arguments.of(
                        listCallback,
                        Collections.emptyMap()
                ),
                Arguments.of(
                        listCallback,
                        Map.of(
                                "name", Collections.emptyList(),
                                "age", Collections.emptyList()
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldThrowExceptionWhenExtraStatementParams(final Function<ProcessingEnvironment, List<ParamInfo>> paramsCallback,
                                                      final Map<String, List<ParamInfo>> classPropertyMap) {
        final var methodValidator = createMethodValidator();
        final var method = ElementFilter.methodsIn(fixture.getEnclosedElements())
                .stream()
                .filter(m -> m.getSimpleName().toString().equals("methodWithExtraStatementParams"))
                .findFirst()
                .orElseThrow();
        final List<ParamInfo> params = paramsCallback.apply(processingEnv);

        final var statementParams = List.of(
                StatementParam.simple("name"),
                StatementParam.simple("userAge")
        );

        assertThatThrownBy(() -> methodValidator.validateParams(method, params, classPropertyMap, statementParams))
                .isInstanceOf(InvalidInputParamException.class)
                .hasMessageContaining("has a follow params not found in method params");
    }

}