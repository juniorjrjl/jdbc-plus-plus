package br.com.jdbcpp.processor.dto.method;

import br.com.jdbcpp.api.Command;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfoFactory;
import br.com.jdbcpp.processor.dto.statement.StatementParam;
import br.com.jdbcpp.processor.exception.InvalidInputParamException;
import br.com.jdbcpp.processor.exception.InvalidMethodSignature;
import br.com.jdbcpp.processor.exception.MoreParamsThanStatementNeed;
import br.com.jdbcpp.processor.util.MethodValidatorUtil;
import br.com.jdbcpp.processor.util.StringUtil;
import com.palantir.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class WriteMethodInfoFactory {

    private WriteMethodInfoFactory() {}

    public static MethodInfo create(final ExecutableElement method,
                                    final List<ParamInfo> params,
                                    final Map<String, List<ParamInfo>> classPropertyMap,
                                    final Command command) throws InvalidMethodSignature{
        final var methodInfo = switch (command.commandType()) {
            case INSERT -> getInsertMethod(method, params, classPropertyMap, command);
            case UPDATE -> getUpdateMethod(method, params, classPropertyMap, command);
            case DELETE -> getDeleteMethod(method, params, classPropertyMap, command);
        };
        MethodValidatorUtil.validateParams(
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
            final var message = String.format("A method DELETE %s must return void or return rows affected", method.getSimpleName());
            throw new InvalidMethodSignature(message);
        }

        MethodValidatorUtil.validateReturn(
                method.getSimpleName().toString(),
                command.returnRowsAffected(),
                deleteMethod.getReturnType(),
                "DELETE",
                List.of(TypeName.VOID)
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

        final List<TypeName> validReturns = params.size() > 1 ?
                List.of(TypeName.VOID) :
                List.of(TypeName.VOID, params.getFirst().getType());
        MethodValidatorUtil.validateReturn(
                method.getSimpleName().toString(),
                command.returnRowsAffected(),
                updateMethod.getReturnType(),
                "UPDATE",
                validReturns
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

        final List<TypeName> validReturns = params.size() > 1 ?
                List.of(TypeName.VOID) :
                List.of(TypeName.VOID, params.getFirst().getType());
        MethodValidatorUtil.validateReturn(
                method.getSimpleName().toString(),
                command.returnRowsAffected(),
                insertMethod.getReturnType(),
                "INSERT",
                validReturns
        );

        return  insertMethod;
    }


}
