package br.com.jdbcpp.processor.service.read.select;

import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import br.com.jdbcpp.processor.service.read.select.result.SelectResultSetDelegator;
import br.com.jdbcpp.processor.service.statement.StatementBuilder;
import com.palantir.javapoet.MethodSpec;

import javax.lang.model.util.Types;
import java.sql.SQLException;

import static javax.lang.model.element.Modifier.PUBLIC;

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
                                    final String connectionCall) {
        final var methodBuilder = MethodSpec.methodBuilder(methodInfo.getName())
                .addException(SQLException.class)
                .addModifiers(PUBLIC);
        final var statementVar = "stmt";
        final var resultSetVar = "rs";
        statementBuilder.build(
                methodBuilder,
                methodInfo,
                "conn",
                connectionCall,
                statementVar,
                resultSetVar
        );
        if (!methodInfo.unParameterizedStatement()) {
            methodBuilder.beginControlFlow("try (final var $N = $N.executeQuery())", resultSetVar, statementVar);
        }
        methodBuilder.beginControlFlow("if ($N.next())", resultSetVar);

        selectResultSetDelegator.build(
                methodInfo,
                "model",
                resultSetVar,
                methodBuilder
        );

        methodBuilder.addStatement("return model")
                .nextControlFlow("else")
                .addStatement("return null")
                .endControlFlow();

        if (!methodInfo.unParameterizedStatement()) {
            methodBuilder.endControlFlow();
        }

        return methodBuilder
                .nextControlFlow("catch (final $T e)", SQLException.class)
                .addStatement("throw e")
                .endControlFlow();
    }

}
