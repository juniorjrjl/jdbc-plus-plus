package br.com.jdbcpp.processor.service.update;

import br.com.jdbcpp.processor.dto.method.UpdateMethod;
import com.palantir.javapoet.MethodSpec;

import java.sql.SQLException;

import static javax.lang.model.element.Modifier.PUBLIC;

public class UpdateMethodGenerator {

    public MethodSpec build(final UpdateMethod updateMethod) {
        final var methodBuilder = MethodSpec.methodBuilder(updateMethod.getName())
                .addModifiers(PUBLIC)
                .returns(updateMethod.getReturnType())
                .addStatement("final var statement = $S", updateMethod.getStatement())
                .beginControlFlow("""
                        try(final var connection = dataSource.getConnection();
                        final var stmt = connection.createStatement())
                        """);
        final var builder = updateMethod.getParams().stream()
                .filter(p -> p.type().equals(updateMethod.getReturnType()))
                .findFirst()
                .map(p -> methodBuilder.addStatement("stmt.executeUpdate(statement);")
                                .addStatement("return $N", p.name()))
                .orElseGet(() -> {
                    if (updateMethod.isReturnRowsAffected()){
                        return methodBuilder.addStatement("return stmt.executeUpdate(statement);");
                    } else {
                        return methodBuilder.addStatement("stmt.executeUpdate(statement);");
                    }
                });
        return builder
                .addStatement("} catch (final $T e) {", SQLException.class)
                .addStatement("throw e;")
                .endControlFlow()
                .build();
    }

}
