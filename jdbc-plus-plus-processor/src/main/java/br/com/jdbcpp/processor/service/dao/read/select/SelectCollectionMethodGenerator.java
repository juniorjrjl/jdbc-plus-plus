package br.com.jdbcpp.processor.service.dao.read.select;

import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.method.SelectCollectionMethodInfo;
import br.com.jdbcpp.processor.service.dao.MethodGenerator;
import br.com.jdbcpp.processor.service.dao.read.select.result.SelectResultSetDelegator;
import br.com.jdbcpp.processor.service.dao.statement.StatementBuilder;
import br.com.jdbcpp.processor.util.CollectionUtil;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

public class SelectCollectionMethodGenerator implements MethodGenerator<SelectCollectionMethodInfo> {

    private final SelectResultSetDelegator selectResultSetDelegator;
    private final StatementBuilder statementBuilder;
    private final CollectionUtil collectionUtil;
    private final TypeName sqlException;

    public SelectCollectionMethodGenerator(final SelectResultSetDelegator selectResultSetDelegator,
                                           final StatementBuilder statementBuilder,
                                           final CollectionUtil collectionUtil,
                                           final TypeMirror sqlException) {
        this.selectResultSetDelegator = selectResultSetDelegator;
        this.statementBuilder = statementBuilder;
        this.collectionUtil = collectionUtil;
        this.sqlException = TypeName.get(sqlException);
    }

    @Override
    public boolean useInstance(final MethodInfo methodInfo) {
        return methodInfo instanceof SelectCollectionMethodInfo;
    }

    @Override
    public MethodSpec.Builder build(final SelectCollectionMethodInfo methodInfo,
                                    final String connectionCall) {
        final var containerReturnTypeMirror = methodInfo.getContainerReturnTypeMirror();
        final var instanceContainer = methodInfo.getInstanceContainer();
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

        final var collectionImpl = collectionUtil.getCollectionImplementation(instanceContainer);
        final var typeImpl = ClassName.bestGuess(collectionImpl);
        methodBuilder.addStatement("final $T result = new $T<>()", containerReturnTypeMirror, typeImpl);
        methodBuilder.beginControlFlow("while ($N.next())", resultSetVar);

        selectResultSetDelegator.build(
                methodInfo,
                "model",
                resultSetVar,
                methodBuilder
        );

        methodBuilder.addStatement("result.add(model)")
                .endControlFlow();

        methodBuilder.addStatement("return result");

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
