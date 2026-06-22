package br.com.jdbcpp.processor.service.read.select;

import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import br.com.jdbcpp.processor.service.read.select.result.SelectResultSetDelegator;
import br.com.jdbcpp.processor.service.statement.StatementBuilder;
import com.palantir.javapoet.MethodSpec;

import javax.lang.model.util.Types;

public class SelectSingleMethodGenerator {

    protected final Types types;
    protected final SelectResultSetDelegator selectResultSetDelegator;
    private final StatementBuilder statementBuilder;

    public SelectSingleMethodGenerator(final Types types,
                                       final SelectResultSetDelegator selectResultSetDelegator,
                                       final StatementBuilder statementBuilder) {
        this.types = types;
        this.selectResultSetDelegator = selectResultSetDelegator;
        this.statementBuilder = statementBuilder;
    }

    public MethodSpec.Builder build(final SelectMethodInfo methodInfo,
                                      final String statementVar) {
        final var methodBuilder = MethodSpec.methodBuilder(methodInfo.getName());
        statementBuilder.build(methodBuilder, methodInfo, "conn");
        methodBuilder.beginControlFlow("final var rs = $N.executeQuery(statement)", statementVar)
                .beginControlFlow("if (rs.next())");
        selectResultSetDelegator.build(
                methodInfo,
                "model",
                "rs",
                methodBuilder);
        return methodBuilder.addStatement("return model;")
                .nextControlFlow("else")
                .addStatement("return null")
                .endControlFlow()
                .endControlFlow();
    }

}
