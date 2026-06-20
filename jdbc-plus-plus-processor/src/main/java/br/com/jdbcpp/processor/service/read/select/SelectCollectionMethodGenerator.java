package br.com.jdbcpp.processor.service.read.select;

import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import br.com.jdbcpp.processor.service.read.ReadSQLStatementMethod;
import br.com.jdbcpp.processor.service.read.select.result.SelectResultSetDelegator;
import br.com.jdbcpp.processor.util.CollectionUtil;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;

import javax.lang.model.util.Types;

import static java.util.Objects.nonNull;

public class SelectCollectionMethodGenerator extends ReadSQLStatementMethod<SelectMethodInfo> {

    private final Types types;
    private final SelectResultSetDelegator selectResultSetDelegator;

    public SelectCollectionMethodGenerator(final Types types, final SelectResultSetDelegator selectResultSetDelegator) {
        this.types = types;
        this.selectResultSetDelegator = selectResultSetDelegator;
    }

    @Override
    protected void buildResultSetRead(final SelectMethodInfo methodInfo,
                                                    final MethodSpec.Builder methodBuilder,
                                                    final String statementVar) {
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

        methodBuilder.addStatement("result.add(model)")
                .endControlFlow()
                .addStatement("return result;")
                .endControlFlow();
    }

}
