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
                                    final String connectionCall) {
        final var methodBuilder = MethodSpec.methodBuilder(methodInfo.getName());
        final var statementVar = "stmt";
        statementBuilder.build(
                methodBuilder,
                methodInfo,
                "conn",
                connectionCall,
                statementVar,
                "rs"
        );
        final var statementCommandVar = statementBuilder.getStatementCommandVar();
        final String executeCall = methodInfo.unParameterizedStatement()
                ? "$N.executeUpdate(" + statementCommandVar + ")"
                : "$N.executeUpdate()";

        methodInfo.getParams().stream()
                .filter(p -> p.getType().equals(methodInfo.getReturnType()))
                .findFirst()
                .map(p -> {
                    methodBuilder.addStatement(executeCall, statementVar);
                    return methodBuilder.addStatement("return $N", p.getName());
                })
                .orElseGet(() -> {
                    if (methodInfo.isReturnRowsAffected()){
                        return methodBuilder.addStatement("return " + executeCall, statementVar);
                    } else {
                        return methodBuilder.addStatement(executeCall, statementVar);
                    }
                });
        return methodBuilder
                .nextControlFlow(" catch (final $T e) ", SQLException.class)
                .addStatement("throw e;")
                .endControlFlow();
    }
}
