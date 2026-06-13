package br.com.jdbcpp.processor.service.select;

import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import br.com.jdbcpp.processor.service.select.result.SelectResultSetDelegator;
import com.palantir.javapoet.MethodSpec;

import javax.lang.model.util.Types;
import java.sql.SQLException;

import static javax.lang.model.element.Modifier.PUBLIC;

public class SelectOptionalMethodGenerator extends SelectMethodGenerator{

    public SelectOptionalMethodGenerator(final Types types,
                                         final SelectResultSetDelegator selectResultSetDelegator) {
        super(types, selectResultSetDelegator);
    }

    public MethodSpec build(final SelectMethodInfo selectMethodInfo) {
        final var methodBuilder = MethodSpec.methodBuilder(selectMethodInfo.getName())
                .addModifiers(PUBLIC)
                .returns(selectMethodInfo.getReturnType())
                .addStatement("final var statement = $S", selectMethodInfo.getStatement())
                .beginControlFlow("""
                        try(final var connection = dataSource.getConnection();
                        final var stmt = connection.createStatement()
                        final var rs = stmt.executeQuery(statement))
                        """)
                .beginControlFlow("if (rs.next())");

        selectResultSetDelegator.build(
                selectMethodInfo,
                "model",
                "rs",
                methodBuilder);
        methodBuilder.addStatement("return $T.of(model)", java.util.Optional.class)
                .nextControlFlow("else")
                .addStatement("return $T.empty()", java.util.Optional.class)
                .endControlFlow()
                .addStatement("return $T.empty()", java.util.Optional.class)
                .nextControlFlow(" catch (final $T e) ", SQLException.class)
                .addStatement("throw e;")
                .endControlFlow();

        return methodBuilder.build();
    }

}
