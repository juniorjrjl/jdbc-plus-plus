package br.com.jdbcpp.processor.dto.method;

import br.com.jdbcpp.api.Command;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfoFactory;
import br.com.jdbcpp.processor.exception.InvalidMethodSignature;
import com.palantir.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.Map;

public final class WriteMethodInfoFactory {

    private WriteMethodInfoFactory() {}

    public static MethodInfo create(final ExecutableElement method,
                                    final List<ParamInfo> params,
                                    final Map<String, List<ParamInfo>> classPropertyMap,
                                    final Command command) throws InvalidMethodSignature{
        return switch (command.commandType()) {
            case INSERT -> getInsertMethod(method, params, classPropertyMap, command);
            case UPDATE -> getUpdateMethod(method, params, classPropertyMap, command);
            case DELETE -> getDeleteMethod(method, params, classPropertyMap, command);
        };
    }

    private static DeleteMethod getDeleteMethod(final ExecutableElement method,
                                                final List<ParamInfo> params,
                                                final Map<String, List<ParamInfo>> classPropertyMap,
                                                final Command command) {
        final var deleteMethod = classPropertyMap.isEmpty() ?
                new DeleteMethod(
                        method.getSimpleName().toString(),
                        method.getReturnType(),
                        params,
                        StatementInfoFactory.create(command.value()),
                        command.returnRowsAffected()
                ) :
                new DeleteMethod(
                        method.getSimpleName().toString(),
                        method.getReturnType(),
                        classPropertyMap,
                        StatementInfoFactory.create(command.value()),
                        command.returnRowsAffected()
                );
        if (!deleteMethod.isReturnRowsAffected() && deleteMethod.getReturnType().equals(TypeName.VOID)) {
            final var message = String.format("A method DELETE %s must be void or return rows affected", method.getSimpleName());
            throw new InvalidMethodSignature(message);
        }

        validateRowsAffected(
                method.getSimpleName().toString(),
                command.returnRowsAffected(),
                deleteMethod.getReturnType(),
                "DELETE"
        );

        return deleteMethod;
    }

    private static UpdateMethod getUpdateMethod(final ExecutableElement method,
                                                final List<ParamInfo> params,
                                                final Map<String, List<ParamInfo>> classPropertyMap,
                                                final Command command) {
        final var updateMethod = classPropertyMap.isEmpty() ?
                new UpdateMethod(
                        method.getSimpleName().toString(),
                        method.getReturnType(),
                        params,
                        StatementInfoFactory.create(command.value()),
                        command.returnRowsAffected()
                ) :
                new UpdateMethod(
                        method.getSimpleName().toString(),
                        method.getReturnType(),
                        classPropertyMap,
                        StatementInfoFactory.create(command.value()),
                        command.returnRowsAffected()
                );

        validateRowsAffected(
                method.getSimpleName().toString(),
                command.returnRowsAffected(),
                updateMethod.getReturnType(),
                "UPDATE"
        );

        return updateMethod;
    }

    private static InsertMethod getInsertMethod(final ExecutableElement method,
                                                final List<ParamInfo> params,
                                                final Map<String, List<ParamInfo>> classPropertyMap,
                                                final Command command) {
        final var insertMethod = classPropertyMap.isEmpty() ?
                new InsertMethod(
                        method.getSimpleName().toString(),
                        method.getReturnType(),
                        params,
                        StatementInfoFactory.create(command.value()),
                        command.returnRowsAffected()
                ) :
                new InsertMethod(
                        method.getSimpleName().toString(),
                        method.getReturnType(),
                        classPropertyMap,
                        StatementInfoFactory.create(command.value()),
                        command.returnRowsAffected()
                );

        validateRowsAffected(
                method.getSimpleName().toString(),
                command.returnRowsAffected(),
                insertMethod.getReturnType(),
                "INSERT"
        );

        return  insertMethod;
    }

    private static void validateRowsAffected(final String method,
                                             final boolean returnRowsAffected,
                                             final TypeName returnType,
                                             final String operation) {
        if (returnRowsAffected && !(returnType.equals(TypeName.LONG) || returnType.equals(TypeName.INT))) {
            final var message = String.format("A method %s %s must return int or long when return rows affected", operation, method);
            throw new InvalidMethodSignature(message);
        }
    }

}
