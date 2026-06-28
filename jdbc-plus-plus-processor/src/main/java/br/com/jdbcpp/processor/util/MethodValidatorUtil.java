package br.com.jdbcpp.processor.util;

import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementParam;
import br.com.jdbcpp.processor.exception.InvalidInputParamException;
import br.com.jdbcpp.processor.exception.InvalidMethodSignatureException;
import br.com.jdbcpp.processor.exception.MoreParamsThanStatementNeedException;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.TypeName;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MethodValidatorUtil {

    private MethodValidatorUtil() {}

    public static void validateReturn(final String method,
                                       final boolean returnRowsAffected,
                                       final TypeName returnType,
                                       final String operation,
                                       final List<TypeName> validReturns) {
        if (returnRowsAffected){
            if (!(returnType.equals(TypeName.LONG)
                    || returnType.equals(TypeName.INT)
                    || returnType.equals(ClassName.get(Long.class))
                    || returnType.equals(ClassName.get(Integer.class)))) {
                final var message = String.format(
                        "A method %s (%s) is defined to return rows affected, but return is not int or long",
                        operation,
                        method
                );
                throw new InvalidMethodSignatureException(message);
            }
        } else {
            if (!validReturns.contains(returnType)) {
                final var message = String.format("""
                        A method %s, without rowsAffected result has invalid config, use a follow configurations:
                         - for INSERT or UPDATE: return void or received class;
                         - for DELETE: return void;
                        """,
                        method
                );
                throw new InvalidMethodSignatureException(message);
            }
        }
    }

    public static void validateParams(final String methodNane,
                                       final List<ParamInfo> params,
                                       final Map<String, List<ParamInfo>> classPropertyMap,
                                       final List<StatementParam> statementParams){
        final var statementParamsNames = statementParams.stream()
                .map(StatementParam::name)
                .map(StringUtil::camelToSnakeCase)
                .collect(Collectors.toSet());
        final var paramsNames = classPropertyMap.isEmpty() ?
                params.stream()
                        .map(ParamInfo::getName)
                        .collect(Collectors.toSet()) :
                classPropertyMap.keySet().stream().toList();

        final var extraInStatement = new HashSet<>(statementParamsNames);
        extraInStatement.removeAll(paramsNames);

        if (!extraInStatement.isEmpty()) {
            final var message = String.format(
                    "A statement used by method %s has a follow params not found in method params: %s",
                    methodNane,
                    extraInStatement
            );
            throw new InvalidInputParamException(message);
        }

        final var missingInStatement = new HashSet<>(paramsNames);
        missingInStatement.removeAll(statementParamsNames);

        if (!missingInStatement.isEmpty()) {
            final var message = String.format(
                    "A method %s received a follow ignored params: %s",
                    methodNane,
                    missingInStatement
            );
            throw new MoreParamsThanStatementNeedException(message);
        }
    }

}
