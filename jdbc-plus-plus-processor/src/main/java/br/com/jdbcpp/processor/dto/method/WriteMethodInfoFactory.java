package br.com.jdbcpp.processor.dto.method;

import br.com.jdbcpp.api.Command;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfoFactory;
import br.com.jdbcpp.processor.exception.InvalidMethodSignatureException;
import br.com.jdbcpp.processor.util.MethodValidator;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Map;

public final class WriteMethodInfoFactory {

    private static final String NONE_EXCEPTION = "br.com.jdbcpp.api.Command.NONE";

    private WriteMethodInfoFactory() {}

    public static MethodInfo create(final ExecutableElement method,
                                    final List<ParamInfo> params,
                                    final Map<String, List<ParamInfo>> classPropertyMap,
                                    final Command command,
                                    final TypeMirror packException) throws InvalidMethodSignatureException {
        final var methodInfo = switch (command.commandType()) {
            case INSERT -> getInsertMethod(method, params, classPropertyMap, command, packException);
            case UPDATE -> getUpdateMethod(method, params, classPropertyMap, command, packException);
            case DELETE -> getDeleteMethod(method, params, classPropertyMap, command, packException);
        };
        MethodValidator.validateParams(
                methodInfo.getName(),
                params,
                classPropertyMap,
                methodInfo.getStatement().params()
        );
        return methodInfo;
    }

    private static DeleteMethod getDeleteMethod(final ExecutableElement method,
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

    private static UpdateMethod getUpdateMethod(final ExecutableElement method,
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

    private static InsertMethod getInsertMethod(final ExecutableElement method,
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
