package br.com.jdbcpp.processor.service.read.select;

import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import br.com.jdbcpp.processor.service.read.select.result.SelectResultSetDelegator;
import br.com.jdbcpp.processor.service.statement.StatementBuilder;
import br.com.jdbcpp.processor.util.CollectionUtil;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;

import javax.lang.model.util.Types;

import static java.util.Objects.nonNull;

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
                                                    final String statementVar) {
        final var methodBuilder = MethodSpec.methodBuilder(methodInfo.getName());
        statementBuilder.build(methodBuilder, methodInfo, "conn");
        final var returnType = methodInfo.getReturnType();
        final var returnTypeMirror = methodInfo.getReturnTypeMirror();
        final var isInterface = CollectionUtil.isCollectionInterface(returnTypeMirror, types);
        final var collectionImpl = CollectionUtil.getCollectionImplementation(returnTypeMirror, types);
        final var elementType = CollectionUtil.getCollectionElementType(returnTypeMirror);
        final var elementTypeName = nonNull(elementType) ? elementType.toString() : "Object";
        methodBuilder.beginControlFlow("final var rs = $N.executeQuery(statement)", statementVar);

        if (isInterface) {
            methodBuilder.addStatement("final $T result = new $T<>()", returnType, ClassName.bestGuess(collectionImpl));
        } else {
            methodBuilder.addStatement("final var result = new $T<>()", ClassName.bestGuess(collectionImpl));
        }
        methodBuilder.beginControlFlow("while (rs.next())")
                .addStatement("final var model = new $T()", ClassName.bestGuess(elementTypeName));

        selectResultSetDelegator.build(
                methodInfo,
                "model",
                "rs",
                methodBuilder);

        return methodBuilder.addStatement("result.add(model)")
                .endControlFlow()
                .addStatement("return result;")
                .endControlFlow();
    }

}
