package br.com.jdbcpp.processor.service.write.update;

import br.com.jdbcpp.processor.dto.method.UpdateMethod;
import br.com.jdbcpp.processor.service.statement.StatementBuilder;
import com.palantir.javapoet.MethodSpec;

import java.sql.SQLException;

public class UpdateMethodGenerator {

    private final StatementBuilder statementBuilder;

    public UpdateMethodGenerator(final StatementBuilder statementBuilder){
        this.statementBuilder = statementBuilder;
    }

    public MethodSpec.Builder build(final UpdateMethod methodInfo,
                                    final String statementVar) {
        final var methodBuilder = MethodSpec.methodBuilder(methodInfo.getName());
        statementBuilder.build(methodBuilder, methodInfo, "conn");
        final var builder = methodInfo.getParams().stream()
                .filter(p -> p.getType().equals(methodInfo.getReturnType()))
                .findFirst()
                .map(p -> methodBuilder.addStatement("$N.executeUpdate(statement);", statementVar)
                        .addStatement("return $N", p.getName()))
                .orElseGet(() -> {
                    if (methodInfo.isReturnRowsAffected()){
                        return methodBuilder.addStatement("return $N.executeUpdate(statement);", statementVar);
                    } else {
                        return methodBuilder.addStatement("$N.executeUpdate(statement);", statementVar);
                    }
                });
        return builder
                .addStatement("} catch (final $T e) {", SQLException.class)
                .addStatement("throw e;")
                .endControlFlow();
    }
}
