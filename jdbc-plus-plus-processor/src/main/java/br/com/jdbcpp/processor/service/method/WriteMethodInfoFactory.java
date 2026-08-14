package br.com.jdbcpp.processor.service.method;

import br.com.jdbcpp.api.Command;
import br.com.jdbcpp.processor.dto.method.DeleteMethod;
import br.com.jdbcpp.processor.dto.method.InsertMethod;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.method.UpdateMethod;
import br.com.jdbcpp.processor.service.statement.StatementInfoFactory;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import static br.com.jdbcpp.api.CommandType.DELETE;
import static br.com.jdbcpp.api.CommandType.INSERT;
import static br.com.jdbcpp.api.CommandType.UPDATE;

public class WriteMethodInfoFactory {

    private final TypeMirror sqlException;
    private final TypeMirror nullWriteException;
    private final Types types;

    public WriteMethodInfoFactory(final TypeMirror sqlException,
                                  final TypeMirror nullWriteException,
                                  final Types types) {
        this.sqlException = sqlException;
        this.nullWriteException = nullWriteException;
        this.types = types;
    }

    public MethodInfo create(final MethodInfo.MethodInfoBuilder builder,
                             final Command command,
                             final TypeMirror packException){
        final var methodExceptionThrow = types.isSameType(nullWriteException, packException) ?
                sqlException:
                packException;
        builder.withPackException(methodExceptionThrow).withStatement(StatementInfoFactory.create(command.value()));
        return switch (command.commandType()) {
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
    }


}
