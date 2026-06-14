package br.com.jdbcpp.processor.service.select;

import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import br.com.jdbcpp.processor.service.select.result.SelectResultSetDelegator;
import br.com.jdbcpp.processor.util.CollectionUtil;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;

import javax.lang.model.util.Types;
import java.sql.SQLException;

import static java.util.Objects.nonNull;
import static javax.lang.model.element.Modifier.PUBLIC;

public class SelectCollectionMethodGenerator extends SelectMethodGenerator{

    public SelectCollectionMethodGenerator(final Types types,
                                           final SelectResultSetDelegator selectResultSetDelegator) {
        super(types, selectResultSetDelegator);
    }

    public MethodSpec build(final SelectMethodInfo selectMethodInfo) {
        final var returnType = selectMethodInfo.getReturnType();
        final var returnTypeMirror = selectMethodInfo.getReturnTypeMirror();
        final var elementType = CollectionUtil.getCollectionElementType(returnTypeMirror);
        final var elementTypeName = nonNull(elementType) ? elementType.toString() : "Object";
        final var collectionImpl = CollectionUtil.getCollectionImplementation(returnTypeMirror, types);
        final var isInterface = CollectionUtil.isCollectionInterface(returnTypeMirror, types);

        final var methodBuilder = MethodSpec.methodBuilder(selectMethodInfo.getName())
                .addModifiers(PUBLIC)
                .returns(returnType)
                .addStatement("final var statement = $S", selectMethodInfo.getStatement())
                .beginControlFlow("""
                        try(final var connection = dataSource.getConnection();
                        final var stmt = connection.createStatement()
                        final var rs = stmt.executeQuery(statement))
                        """);

        if (isInterface) {
            methodBuilder.addStatement("final $T result = new $T<>()", returnType, ClassName.bestGuess(collectionImpl));
        } else {
            methodBuilder.addStatement("final var result = new $T<>()", ClassName.bestGuess(collectionImpl));
        }
        methodBuilder.beginControlFlow("while (rs.next())")
                .addStatement("final var model = new $T()", ClassName.bestGuess(elementTypeName));

        selectResultSetDelegator.build(
                selectMethodInfo,
                "model",
                "rs",
                methodBuilder);

        methodBuilder.addStatement("result.add(model)")
                .endControlFlow()
                .addStatement("return result;")
                .nextControlFlow(" catch (final $T e) ", SQLException.class)
                .addStatement("throw e;")
                .endControlFlow();

        return methodBuilder.build();
    }

}
