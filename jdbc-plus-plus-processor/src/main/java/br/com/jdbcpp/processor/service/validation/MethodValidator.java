package br.com.jdbcpp.processor.service.validation;

import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementParam;
import br.com.jdbcpp.processor.exception.InvalidInputParamException;
import br.com.jdbcpp.processor.exception.InvalidMethodSignatureException;
import br.com.jdbcpp.processor.exception.MoreParamsThanStatementNeedException;
import br.com.jdbcpp.processor.util.StringUtil;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class MethodValidator {

    private final List<TypeMirror> allowedReturnsRowsAffected;
    private final Types types;
    private final TypeMirror throwable;
    private final TypeMirror sqlException;

    public MethodValidator(final Types types,
                           final TypeMirror longType,
                           final TypeMirror integerType,
                           final TypeMirror throwable,
                           final TypeMirror sqlException) {
        allowedReturnsRowsAffected = List.of(
                longType,
                integerType,
                types.getPrimitiveType(TypeKind.LONG),
                types.getPrimitiveType(TypeKind.INT)
        );
        this.types = types;
        this.throwable = throwable;
        this.sqlException = sqlException;
    }

    public void validateReturn(final ExecutableElement method,
                               final boolean returnRowsAffected,
                               final String operation,
                               final List<TypeMirror> validReturns) throws InvalidMethodSignatureException {
        final var returnType = method.getReturnType();
        if (returnRowsAffected){
            if (!allowedReturnsRowsAffected.contains(returnType)) {
                final var message = String.format(
                        "A method %s (%s) is defined to return rows affected, but return is not int or long",
                        operation,
                        method.getSimpleName()
                );
                throw new InvalidMethodSignatureException(message, method);
            }
        } else {
            if (!validReturns.contains(returnType)) {
                final var message = String.format("""
                        A method %s, without rowsAffected result has invalid config, use a follow configurations:
                         - for INSERT or UPDATE: return void or received class;
                         - for DELETE: return void;
                        """,
                        method.getSimpleName()
                );
                throw new InvalidMethodSignatureException(message, method);
            }
        }
    }

    public void validateParams(final ExecutableElement method,
                               final List<ParamInfo> params,
                               final Map<String, List<ParamInfo>> classPropertyMap,
                               final List<StatementParam> statementParams) throws InvalidInputParamException,
            MoreParamsThanStatementNeedException {
        final var statementParamsNames = statementParams.stream()
                .map(StatementParam::name)
                .map(StringUtil::camelToSnakeCase)
                .collect(Collectors.toSet());
        final var paramsNames = classPropertyMap.isEmpty() ?
                params.stream()
                        .filter(SimpleParamInfo.class::isInstance)
                        .map(SimpleParamInfo.class::cast)
                        .map(p -> p.getName().equals(p.getQueryParamName()) ?
                                p.getName() :
                                p.getQueryParamName())
                        .collect(Collectors.toSet()) :
                classPropertyMap.keySet().stream().map(StringUtil::camelToSnakeCase).toList();

        final var extraInStatement = new HashSet<>(statementParamsNames);
        extraInStatement.removeAll(paramsNames);

        if (!extraInStatement.isEmpty()) {
            final var message = String.format(
                    "A statement used by method %s has a follow params not found in method params: %s",
                    method.getSimpleName(),
                    extraInStatement
            );
            throw new InvalidInputParamException(message, method);
        }

        final var missingInStatement = new HashSet<>(paramsNames);
        missingInStatement.removeAll(statementParamsNames);

        if (!missingInStatement.isEmpty()) {
            final var message = String.format(
                    "A method %s received a follow ignored params: %s",
                    method.getSimpleName(),
                    missingInStatement
            );
            throw new MoreParamsThanStatementNeedException(message, method);
        }
    }

    public void validateExceptionThrow(final ExecutableElement method,
                                       final TypeMirror exception) throws InvalidMethodSignatureException{
        if (types.isSameType(exception, sqlException)) {
            return;
        }

        final var requiredConstructorNotFound = Optional.of(types.asElement(exception))
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .map(TypeElement::getEnclosedElements)
                .map(ElementFilter::constructorsIn)
                .stream()
                .flatMap(List::stream)
                .noneMatch(c -> c.getParameters().size() == 1
                        && types.isSameType(c.getParameters().getFirst().asType(), throwable));

        if (requiredConstructorNotFound){
            final var message = String.format(
                    "For method %s throw a custom exception %s create a constructor with a Throwable parameter ",
                    method.getSimpleName(),
                    exception
            );
            throw new InvalidMethodSignatureException(message, method);
        }


    }

}
