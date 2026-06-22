package br.com.jdbcpp.processor.service.write.delete;

import br.com.jdbcpp.processor.dto.method.DeleteMethod;
import br.com.jdbcpp.processor.service.statement.StatementBuilder;
import com.palantir.javapoet.MethodSpec;

import java.sql.SQLException;

public class DeleteMethodGenerator {

    private final StatementBuilder statementBuilder;

    public DeleteMethodGenerator(final StatementBuilder statementBuilder){
        this.statementBuilder = statementBuilder;
    }

    public MethodSpec.Builder build(final DeleteMethod methodInfo,
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
        final var executeCall = methodInfo.unParameterizedStatement() ?
                "$N.executeUpdate(" + statementCommandVar + ")" :
                "$N.executeUpdate()";


        if (methodInfo.isReturnRowsAffected()){
            methodBuilder.addStatement("return " + executeCall, statementVar);
        } else {
            methodBuilder.addStatement(executeCall, statementVar);
        }

        return methodBuilder
                .addStatement("} catch (final $T e) {", SQLException.class)
                .addStatement("throw e")
                .endControlFlow();
    }

}
