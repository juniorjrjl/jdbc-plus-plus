package br.com.jdbcpp.processor.service.write.insert;

import br.com.jdbcpp.processor.dto.method.InsertMethod;
import br.com.jdbcpp.processor.service.write.WriteSQLStatementMethod;
import com.palantir.javapoet.MethodSpec;

import java.sql.SQLException;

public class InsertMethodGenerator extends WriteSQLStatementMethod<InsertMethod> {

    @Override
    protected MethodSpec.Builder buildBaseBody(final InsertMethod methodInfo,
                                               final MethodSpec.Builder methodBuilder,
                                               final String statementVar) {
        final var builder = methodInfo.getParams().stream()
                .filter(p -> p.getType().equals(methodInfo.getReturnType()))
                .findFirst()
                .map(p -> methodBuilder.addStatement("$N.executeUpdate(statement);", statementVar)
                        .addStatement("return $N", p.getName()))
                .orElseGet(() -> {
                    if (methodInfo.isReturnRowsAffected()){
                        return methodBuilder.addStatement("return $N.executeUpdate(statement)", statementVar);
                    } else {
                        return methodBuilder.addStatement("$N.executeUpdate(statement)", statementVar);
                    }
                });
        return builder
                .addStatement("} catch (final $T e) {", SQLException.class)
                .addStatement("throw e;")
                .endControlFlow();
    }

}
