package br.com.jdbcpp.processor.service.read.select;

import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import br.com.jdbcpp.processor.service.read.ReadSQLStatementMethod;
import br.com.jdbcpp.processor.service.read.select.result.SelectResultSetDelegator;
import com.palantir.javapoet.MethodSpec;

import javax.lang.model.util.Types;

public class SelectSingleMethodGenerator extends ReadSQLStatementMethod<SelectMethodInfo> {

    protected final Types types;
    protected final SelectResultSetDelegator selectResultSetDelegator;

    public SelectSingleMethodGenerator(final Types types,
                                       final SelectResultSetDelegator selectResultSetDelegator) {
        this.types = types;
        this.selectResultSetDelegator = selectResultSetDelegator;
    }

    @Override
    protected void buildResultSetRead(final SelectMethodInfo methodInfo, final MethodSpec.Builder methodBuilder, final String statementVar) {
        methodBuilder.beginControlFlow("final var rs = $N.executeQuery(statement)", statementVar)
                .beginControlFlow("if (rs.next())");
        selectResultSetDelegator.build(
                methodInfo,
                "model",
                "rs",
                methodBuilder);
        methodBuilder.addStatement("return model;")
                .nextControlFlow("else")
                .addStatement("return null")
                .endControlFlow()
                .endControlFlow();
    }

}
