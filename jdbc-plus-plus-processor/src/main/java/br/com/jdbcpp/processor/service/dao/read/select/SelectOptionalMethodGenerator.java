package br.com.jdbcpp.processor.service.dao.read.select;

import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.method.SelectOptionalMethodInfo;
import br.com.jdbcpp.processor.service.dao.MethodGenerator;
import br.com.jdbcpp.processor.service.dao.read.select.result.SelectResultSetDelegator;
import br.com.jdbcpp.processor.service.dao.statement.StatementBuilder;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

public class SelectOptionalMethodGenerator implements MethodGenerator<SelectOptionalMethodInfo> {

    protected final SelectResultSetDelegator selectResultSetDelegator;
    private final StatementBuilder statementBuilder;
    private final TypeName sqlException;

    public SelectOptionalMethodGenerator(final SelectResultSetDelegator selectResultSetDelegator,
                                         final StatementBuilder statementBuilder,
                                         final TypeMirror sqlException) {
        this.selectResultSetDelegator = selectResultSetDelegator;
        this.statementBuilder = statementBuilder;
        this.sqlException = TypeName.get(sqlException);
    }

    @Override
    public boolean useInstance(final MethodInfo methodInfo) {
        return methodInfo instanceof SelectOptionalMethodInfo;
    }

    @Override
    public MethodSpec.Builder build(final SelectOptionalMethodInfo methodInfo,
                                    final String connectionCall) {
        final var containerReturnTypeMirror = methodInfo.getContainerReturnTypeMirror();
        final var containerReturnType = TypeName.get(containerReturnTypeMirror);
        final var methodBuilder = MethodSpec.methodBuilder(methodInfo.getName())
                .addModifiers(PUBLIC)
                .returns(containerReturnType);

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

        methodBuilder.addStatement("return $T.of(model)", Optional.class)
                .nextControlFlow("else")
                .addStatement("return $T.empty()", Optional.class)
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
