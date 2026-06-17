package br.com.jdbcpp.processor.service.delete;

import br.com.jdbcpp.processor.dto.method.DeleteMethod;
import com.palantir.javapoet.MethodSpec;

import java.sql.SQLException;

import static javax.lang.model.element.Modifier.PUBLIC;

public class DeleteMethodGenerator {

    public MethodSpec build(final DeleteMethod deleteMethod) {
        final var methodBuilder = MethodSpec.methodBuilder(deleteMethod.getName())
                .addModifiers(PUBLIC)
                .returns(deleteMethod.getReturnType())
                .addStatement("final var statement = $S", deleteMethod.getStatement())
                .beginControlFlow("""
                        try(final var connection = dataSource.getConnection();
                        final var stmt = connection.createStatement())
                        """);
        if (deleteMethod.isReturnRowsAffected()){
            methodBuilder.addStatement("return stmt.executeUpdate(statement)");
        } else {
            methodBuilder.addStatement("stmt.executeUpdate(statement)");
        }

        return methodBuilder
                .addStatement("} catch (final $T e) {", SQLException.class)
                .addStatement("throw e")
                .endControlFlow()
                .build();
    }

}
