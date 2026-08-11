package br.com.jdbcpp.processor.service.method;

import br.com.jdbcpp.api.Command;
import br.com.jdbcpp.processor.dto.method.DeleteMethod;
import br.com.jdbcpp.processor.dto.method.InsertMethod;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.method.UpdateMethod;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.exception.InvalidInputParamException;
import br.com.jdbcpp.processor.exception.InvalidMethodSignatureException;
import br.com.jdbcpp.processor.exception.MoreParamsThanStatementNeedException;
import br.com.jdbcpp.processor.service.statement.StatementInfoFactory;
import br.com.jdbcpp.processor.service.validation.MethodValidator;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Map;

import static br.com.jdbcpp.api.CommandType.DELETE;
import static br.com.jdbcpp.api.CommandType.INSERT;
import static br.com.jdbcpp.api.CommandType.UPDATE;

public class WriteMethodInfoFactory {

    private final MethodValidator methodValidator;
    private final TypeMirror sqlException;
    private final TypeMirror nullWriteException;
    private final Types types;

    public WriteMethodInfoFactory(final MethodValidator methodValidator,
                                  final TypeMirror sqlException,
                                  final TypeMirror nullWriteException,
                                  final Types types) {
        this.methodValidator = methodValidator;
        this.sqlException = sqlException;
        this.nullWriteException = nullWriteException;
        this.types = types;
    }

    public MethodInfo create(final ExecutableElement method,
                             final List<ParamInfo> params,
                             final Map<String, List<ParamInfo>> classPropertyMap,
                             final Command command,
                             final TypeMirror packException) throws InvalidMethodSignatureException,
            InvalidInputParamException,
            MoreParamsThanStatementNeedException {
        final var methodExceptionThrow = types.isSameType(nullWriteException, packException) ?
                sqlException:
                packException;
        final var builder = MethodInfo.builder()
                .withName(method.getSimpleName().toString())
                .withReturnType(method.getReturnType())
                .withParams(params)
                .withClassPropertyMap(classPropertyMap)
                .withPackException(methodExceptionThrow)
                .withStatement(StatementInfoFactory.create(command.value()));
        final var methodInfo = switch (command.commandType()) {
            case INSERT -> builder.<InsertMethod.InsertMethodBuilder>asWriteType(INSERT)
                    .withReturnRowsAffected(command.returnRowsAffected())
                    .build();
            case UPDATE -> builder.<UpdateMethod.UpdateMethodBuilder>asWriteType(UPDATE)
                    .withReturnRowsAffected(command.returnRowsAffected())
                    .build();
            case DELETE -> builder.<DeleteMethod.DeleteMethodBuilder>asWriteType(DELETE)
                    .withReturnRowsAffected(command.returnRowsAffected())
                    .build();
        };
        methodValidator.validateParams(
                method,
                params,
                classPropertyMap,
                methodInfo.getStatement().params()
        );
        methodValidator.validateExceptionThrow(method, methodExceptionThrow);
        return methodInfo;
    }


}
