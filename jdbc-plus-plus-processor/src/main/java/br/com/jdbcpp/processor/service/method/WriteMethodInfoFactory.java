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
        final var methodInfo = switch (command.commandType()) {
            case INSERT -> getInsertMethod(method, params, classPropertyMap, command, methodExceptionThrow);
            case UPDATE -> getUpdateMethod(method, params, classPropertyMap, command, methodExceptionThrow);
            case DELETE -> getDeleteMethod(method, params, classPropertyMap, command, methodExceptionThrow);
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

    private DeleteMethod getDeleteMethod(final ExecutableElement method,
                                         final List<ParamInfo> params,
                                         final Map<String, List<ParamInfo>> classPropertyMap,
                                         final Command command,
                                         final TypeMirror packException) {
        return new DeleteMethod(
                method.getSimpleName().toString(),
                method.getReturnType(),
                params,
                classPropertyMap,
                StatementInfoFactory.create(command.value()),
                packException,
                command.returnRowsAffected()
        );
    }

    private UpdateMethod getUpdateMethod(final ExecutableElement method,
                                         final List<ParamInfo> params,
                                         final Map<String, List<ParamInfo>> classPropertyMap,
                                         final Command command,
                                         final TypeMirror packException) {
        return new UpdateMethod(
                method.getSimpleName().toString(),
                method.getReturnType(),
                params,
                classPropertyMap,
                StatementInfoFactory.create(command.value()),
                packException,
                command.returnRowsAffected()
        );
    }

    private InsertMethod getInsertMethod(final ExecutableElement method,
                                         final List<ParamInfo> params,
                                         final Map<String, List<ParamInfo>> classPropertyMap,
                                         final Command command,
                                         final TypeMirror packException) {
        return new InsertMethod(
                method.getSimpleName().toString(),
                method.getReturnType(),
                params,
                classPropertyMap,
                StatementInfoFactory.create(command.value()),
                packException,
                command.returnRowsAffected()
        );
    }


}
