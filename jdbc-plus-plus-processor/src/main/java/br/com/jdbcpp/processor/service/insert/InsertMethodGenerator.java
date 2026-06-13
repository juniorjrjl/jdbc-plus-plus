package br.com.jdbcpp.processor.service.insert;

import br.com.jdbcpp.processor.dto.method.InsertMethod;
import com.palantir.javapoet.MethodSpec;

import java.sql.SQLException;

import static javax.lang.model.element.Modifier.PUBLIC;

public class InsertMethodGenerator {

    public MethodSpec build(final InsertMethod insertMethod) {
        final var methodBuilder = MethodSpec.methodBuilder(insertMethod.getName())
                .addModifiers(PUBLIC)
                .returns(insertMethod.getReturnType())
                .addStatement("final var statement = $S", insertMethod.getStatement())
                .beginControlFlow("""
                        try(final var connection = dataSource.getConnection();
                        final var stmt = connection.createStatement())
                        """);
        final var builder = insertMethod.getParams().stream()
                .filter(p -> p.type().equals(insertMethod.getReturnType()))
                .findFirst()
                .map(p -> methodBuilder.addStatement("stmt.executeUpdate(statement);")
                        .addStatement("return $N", p.name()))
                .orElseGet(() -> {
                    if (insertMethod.isReturnRowsAffected()){
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
