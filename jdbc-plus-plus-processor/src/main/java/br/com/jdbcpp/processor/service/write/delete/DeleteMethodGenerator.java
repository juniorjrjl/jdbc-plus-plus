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
                                      final String statementVar) {
        final var methodBuilder = MethodSpec.methodBuilder(methodInfo.getName());
        statementBuilder.build(methodBuilder, methodInfo, "conn");
        if (methodInfo.isReturnRowsAffected()){
            methodBuilder.addStatement("return $N.executeUpdate(statement)", statementVar);
        } else {
            methodBuilder.addStatement("$N.executeUpdate(statement)", statementVar);
        }

        return methodBuilder
                .addStatement("} catch (final $T e) {", SQLException.class)
                .addStatement("throw e")
                .endControlFlow();
    }

}
