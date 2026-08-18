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

import static org.assertj.core.api.Assertions.assertThatCode;
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
                Arguments.of("methodReturningInt", "INSERT", true),
                Arguments.of("methodReturningInteger", "INSERT", true),
                Arguments.of("methodReturningLongWrapper", "INSERT", true),
                Arguments.of("methodReturningLong", "INSERT", true),

                Arguments.of("methodReturningInt", "UPDATE", false),
                Arguments.of("methodReturningInteger", "UPDATE", false),
                Arguments.of("methodReturningLongWrapper", "UPDATE", false),
                Arguments.of("methodReturningLong", "UPDATE", false),

                Arguments.of("methodReturningInt", "DELETE", false),
                Arguments.of("methodReturningInteger", "DELETE", false),
                Arguments.of("methodReturningLongWrapper", "DELETE", false),
                Arguments.of("methodReturningLong", "DELETE", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldValidateReturnWhenRowsAffectedTrue(final String methodName,
                                                  final String operation,
                                                  final boolean isInsert) {
        final var methodValidator = createMethodValidator();
        final var method = ElementFilter.methodsIn(fixture.getEnclosedElements())
                .stream()
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .findFirst()
                .orElseThrow();

        assertThatNoException().isThrownBy(() ->
                methodValidator.validateWriteReturn(
                        method,
                        Collections.emptyMap(),
                        true,
                        operation,
                        isInsert
                ));
    }

    private static Stream<Arguments> shouldThrowExceptionWhenRowsAffectedTrueAndInvalidReturn() {
        return Stream.of(
                Arguments.of("methodReturningString", "INSERT", true),
                Arguments.of("methodReturningObject", "UPDATE", false),
                Arguments.of("methodReturningVoid", "DELETE", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldThrowExceptionWhenRowsAffectedTrueAndInvalidReturn(final String methodName,
                                                                  final String operation,
                                                                  final boolean isInsert) {
        final var methodValidator = createMethodValidator();
        final var method = ElementFilter.methodsIn(fixture.getEnclosedElements())
                .stream()
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .findFirst()
                .orElseThrow();

        assertThatThrownBy(() -> methodValidator.validateWriteReturn(
                method,
                Collections.emptyMap(),
                true,
                operation,
                isInsert
                ))
                .isInstanceOf(InvalidMethodSignatureException.class)
                .hasMessageContaining("is defined to return rows affected, but return is not int or long");
    }

    private static Stream<Arguments> shouldValidateReturnWhenRowsAffectedFalse() {
        return Stream.of(
                Arguments.of("methodReturningVoid", "INSERT", Collections.emptyMap(), true),
                Arguments.of("methodReturningStringWithClassParam", "INSERT", Map.of("param", List.of()), true),
                Arguments.of("methodReturningVoid", "UPDATE", Collections.emptyMap(), false),
                Arguments.of("methodReturningStringWithClassParam", "UPDATE", Map.of("param", List.of()), false),
                Arguments.of("methodReturningVoid", "DELETE", Collections.emptyMap(), false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldValidateReturnWhenRowsAffectedFalse(final String methodName,
                                                   final String operation,
                                                   final Map<String, List<ParamInfo>> classPropertyMap,
                                                   final boolean isInsert) {
        final var methodValidator = createMethodValidator();
        final var method = ElementFilter.methodsIn(fixture.getEnclosedElements())
                .stream()
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .findFirst()
                .orElseThrow();

        assertThatNoException().isThrownBy(() ->
                methodValidator.validateWriteReturn(
                        method,
                        classPropertyMap,
                        false,
                        operation,
                        isInsert
                        )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"methodReturningInt", "methodReturningLong", "methodReturningInteger", "methodReturningLongWrapper"})
    void shouldDoesNotThrowExceptionWhenRowsAffectedFalseAndInvalidReturn(final String methodName) {
        final var methodValidator = createMethodValidator();
        final var method = ElementFilter.methodsIn(fixture.getEnclosedElements())
                .stream()
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .findFirst()
                .orElseThrow();

        assertThatCode(() -> methodValidator.validateWriteReturn(
                method,
                Collections.emptyMap(),
                false,
                "INSERT",
                true
        )).doesNotThrowAnyException();
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

        assertThatThrownBy(() -> methodValidator.validateWriteReturn(
                method,
                Collections.emptyMap(),
                false,
                "UPDATE",
                false
        ))
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
        final Function<ProcessingEnvironment, Map<String, List<ParamInfo>>> emptyMapCallback = env -> Collections.emptyMap();
        final Function<ProcessingEnvironment, Map<String, List<ParamInfo>>> classPropertyMapCallback = env -> Map.of(
                "name", List.of(
                        SimpleParamInfo.builder()
                                .withName("name")
                                .withType(env.getElementUtils().getTypeElement("java.lang.String").asType())
                                .withCustomEnum(true)
                                .withQueryParamName("name")
                                .withConvertMethod("getName")
                                .build()
                ),
                "age", List.of(
                        SimpleParamInfo.builder()
                                .withName("age")
                                .withType(env.getElementUtils().getTypeElement("java.lang.Integer").asType())
                                .withCustomEnum(true)
                                .withQueryParamName("age")
                                .withConvertMethod("getAge")
                                .build()
                ),
                "email", List.of(
                        SimpleParamInfo.builder()
                                .withName("email")
                                .withType(env.getElementUtils().getTypeElement("java.lang.String").asType())
                                .withCustomEnum(true)
                                .withQueryParamName("email")
                                .withConvertMethod("getEmail")
                                .build()
                )
        );
        return Stream.of(
                Arguments.of(
                        listCallback,
                        emptyMapCallback
                ),
                Arguments.of(
                        listCallback,
                        classPropertyMapCallback
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldValidateParamsWhenMatching(final Function<ProcessingEnvironment, List<ParamInfo>> paramsCallback,
                                          final Function<ProcessingEnvironment, Map<String, List<ParamInfo>>> classPropertyMapCallback) {
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
                methodValidator.validateParams(method, params, classPropertyMapCallback.apply(processingEnv), statementParams));
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
        final Function<ProcessingEnvironment, Map<String, List<ParamInfo>>> emptyMapCallback = env -> Collections.emptyMap();
        final Function<ProcessingEnvironment, Map<String, List<ParamInfo>>> classPropertyMapCallback = env -> Map.of(
                "name", List.of(
                        SimpleParamInfo.builder()
                                .withName("name")
                                .withType(env.getElementUtils().getTypeElement("java.lang.String").asType())
                                .withCustomEnum(true)
                                .withQueryParamName("name")
                                .withConvertMethod("getName")
                                .build()
                ),
                "age", List.of(
                        SimpleParamInfo.builder()
                                .withName("age")
                                .withType(env.getElementUtils().getTypeElement("java.lang.Integer").asType())
                                .withCustomEnum(true)
                                .withQueryParamName("age")
                                .withConvertMethod("getAge")
                                .build()
                ),
                "email", List.of(
                        SimpleParamInfo.builder()
                                .withName("email")
                                .withType(env.getElementUtils().getTypeElement("java.lang.Integer").asType())
                                .withCustomEnum(true)
                                .withQueryParamName("email")
                                .withConvertMethod("getEmail")
                                .build()
                )
        );
        return Stream.of(
                Arguments.of(
                        listCallback,
                        emptyMapCallback
                ),
                Arguments.of(
                        listCallback,
                        classPropertyMapCallback
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldThrowExceptionWhenExtraMethodParams(final Function<ProcessingEnvironment, List<ParamInfo>> paramsCallback,
                                                   final Function<ProcessingEnvironment, Map<String, List<ParamInfo>>> classPropertyMapCallback) {
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

        assertThatThrownBy(() -> methodValidator.validateParams(method, params, classPropertyMapCallback.apply(processingEnv), statementParams))
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
        final Function<ProcessingEnvironment, Map<String, List<ParamInfo>>> emptyMapCallback = env -> Collections.emptyMap();
        final Function<ProcessingEnvironment, Map<String, List<ParamInfo>>> classPropertyMapCallback = env -> Map.of(
                "name", List.of(
                        SimpleParamInfo.builder()
                                .withName("name")
                                .withType(env.getElementUtils().getTypeElement("java.lang.String").asType())
                                .withCustomEnum(true)
                                .withQueryParamName("name")
                                .withConvertMethod("getName")
                                .build()
                ),
                "age", List.of(
                        SimpleParamInfo.builder()
                                .withName("age")
                                .withType(env.getElementUtils().getTypeElement("java.lang.String").asType())
                                .withCustomEnum(true)
                                .withQueryParamName("age")
                                .withConvertMethod("getAge")
                                .build()
                )
        );
        return Stream.of(
                Arguments.of(
                        listCallback,
                        emptyMapCallback
                ),
                Arguments.of(
                        listCallback,
                        classPropertyMapCallback
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldThrowExceptionWhenExtraStatementParams(final Function<ProcessingEnvironment, List<ParamInfo>> paramsCallback,
                                                      final Function<ProcessingEnvironment, Map<String, List<ParamInfo>>> classPropertyMapCallback) {
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

        assertThatThrownBy(() -> methodValidator.validateParams(method, params, classPropertyMapCallback.apply(processingEnv), statementParams))
                .isInstanceOf(InvalidInputParamException.class)
                .hasMessageContaining("has a follow params not found in method params");
    }

}