package br.com.jdbcpp.processor.service.write.insert;

import br.com.jdbcpp.processor.dto.method.InsertMethod;
import br.com.jdbcpp.processor.service.statement.StatementBuilder;
import com.palantir.javapoet.MethodSpec;

import java.sql.SQLException;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

public class InsertMethodGenerator {

    private final StatementBuilder statementBuilder;

    public InsertMethodGenerator(final StatementBuilder statementBuilder){
        this.statementBuilder = statementBuilder;
    }

    public MethodSpec.Builder build(final InsertMethod methodInfo,
                                    final String connectionCall) {
        final var methodBuilder = MethodSpec.methodBuilder(methodInfo.getName())
                .addException(SQLException.class)
                .addModifiers(PUBLIC)
                .returns(methodInfo.getReturnType());

        methodInfo.getParams().forEach(p -> methodBuilder.addParameter(p.getType(), p.getName(), FINAL));

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
                .ifPresentOrElse(
                        p -> {
                            methodBuilder.addStatement(executeCall, statementVar);
                            methodBuilder.addStatement("return $N", p.getName());
                        },
                        () -> {
                            if (methodInfo.isReturnRowsAffected()){
                                methodBuilder.addStatement("return " + executeCall, statementVar);
                            } else {
                                methodBuilder.addStatement(executeCall, statementVar);
                            }
                        }
                );
        return methodBuilder
                .nextControlFlow(" catch (final $T e) ", SQLException.class)
                .addStatement("throw e;")
                .endControlFlow();
    }

}
