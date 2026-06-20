package br.com.jdbcpp.processor.service.write.delete;

import br.com.jdbcpp.processor.dto.method.DeleteMethod;
import br.com.jdbcpp.processor.service.write.WriteSQLStatementMethod;
import com.palantir.javapoet.MethodSpec;

import java.sql.SQLException;

public class DeleteMethodGenerator extends WriteSQLStatementMethod<DeleteMethod> {

    @Override
    protected MethodSpec.Builder buildBaseBody(final DeleteMethod methodInfo,
                                               final MethodSpec.Builder methodBuilder,
                                               final String statementVar) {
        if (methodInfo.isReturnRowsAffected()){
            methodBuilder.addStatement("return $N.executeUpdate(statement)", statementVar);
        } else {
            methodBuilder.addStatement("$N.executeUpdate(statement)", statementVar);
        }

        return methodBuilder
                .addStatement("} catch (final $T e) {", SQLException.class)
                .addStatement("throw e")
                .endControlFlow();
    }

}
