package br.com.jdbcpp.processor.service.read.select;

import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import br.com.jdbcpp.processor.service.read.select.result.SelectResultSetDelegator;
import br.com.jdbcpp.processor.service.statement.StatementBuilder;
import br.com.jdbcpp.processor.util.CollectionUtil;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;

import javax.lang.model.util.Types;
import java.sql.SQLException;

import static java.util.Objects.requireNonNull;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

public class SelectCollectionMethodGenerator {

    private final Types types;
    private final SelectResultSetDelegator selectResultSetDelegator;
    private final StatementBuilder statementBuilder;

    public SelectCollectionMethodGenerator(final Types types,
                                           final SelectResultSetDelegator selectResultSetDelegator,
                                           final StatementBuilder statementBuilder) {
        this.types = types;
        this.selectResultSetDelegator = selectResultSetDelegator;
        this.statementBuilder = statementBuilder;
    }

    public MethodSpec.Builder build(final SelectMethodInfo methodInfo,
                                    final String connectionCall) {
        final var containerReturnTypeMirror = requireNonNull(
                methodInfo.getContainerReturnTypeMirror(),
                "For collection method, container return type mirror must not be null"
        );
        final var instanceContainer = requireNonNull(
                methodInfo.getInstanceContainer(),
                "For collection method, container instance type mirror must not be null"
        );
        final var containerReturnType = TypeName.get(containerReturnTypeMirror);
        final var methodBuilder = MethodSpec.methodBuilder(methodInfo.getName())
                .addException(SQLException.class)
                .addModifiers(PUBLIC)
                .returns(containerReturnType);

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

        final var collectionImpl = CollectionUtil.getCollectionImplementation(instanceContainer, types);
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

        return methodBuilder
                .nextControlFlow("catch (final $T e)", SQLException.class)
                .addStatement("throw e")
                .endControlFlow();
    }

}
