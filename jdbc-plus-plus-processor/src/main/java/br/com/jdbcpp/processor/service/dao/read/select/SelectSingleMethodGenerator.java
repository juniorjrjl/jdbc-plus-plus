package br.com.jdbcpp.processor.service.dao.read.select;

import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import br.com.jdbcpp.processor.service.dao.read.select.result.SelectResultSetDelegator;
import br.com.jdbcpp.processor.service.dao.statement.StatementBuilder;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

public class SelectSingleMethodGenerator {

    protected final SelectResultSetDelegator selectResultSetDelegator;
    private final StatementBuilder statementBuilder;
    private final TypeName sqlException;

    public SelectSingleMethodGenerator(final SelectResultSetDelegator selectResultSetDelegator,
                                       final StatementBuilder statementBuilder,
                                       final TypeMirror sqlException) {
        this.selectResultSetDelegator = selectResultSetDelegator;
        this.statementBuilder = statementBuilder;
        this.sqlException = TypeName.get(sqlException);
    }

    public MethodSpec.Builder build(final SelectMethodInfo methodInfo,
                                    final String connectionCall) {
        final var methodBuilder = MethodSpec.methodBuilder(methodInfo.getName())
                .addModifiers(PUBLIC)
                .returns(TypeName.get(methodInfo.getReturnType()));

        final var receivedException = TypeName.get(methodInfo.getPackException());
        if (receivedException.equals(sqlException)){
            methodBuilder.addException(sqlException);
        }

        methodInfo.getParams().forEach(p -> methodBuilder.addParameter(TypeName.get(p.getType()), p.getName(), FINAL));

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

        methodBuilder.nextControlFlow("catch (final $T e)", sqlException);

        if (receivedException.equals(sqlException)){
            methodBuilder.addStatement("throw e");
        } else {
            methodBuilder.addStatement("throw new $T(e)", receivedException);
        }

        return methodBuilder.endControlFlow();
    }

}
