package br.com.jdbcpp.processor.service.dao.write.delete;

import br.com.jdbcpp.processor.dto.method.DeleteMethod;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.service.dao.statement.StatementBuilder;
import br.com.jdbcpp.processor.service.dao.write.WriteMethodGenerator;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;

public class DeleteMethodGenerator extends WriteMethodGenerator<DeleteMethod> {

    public DeleteMethodGenerator(final StatementBuilder statementBuilder,
                                 final TypeMirror sqlException){
        super(statementBuilder, sqlException);
    }

    @Override
    public boolean useInstance(final MethodInfo methodInfo) {
        return methodInfo instanceof DeleteMethod;
    }

    @Override
    public MethodSpec.Builder build(final DeleteMethod methodInfo,
                                    final String connectionCall) {
        final var receivedException = TypeName.get(methodInfo.getPackException());
        final var methodBuilder = buildMethodSignature(methodInfo, receivedException);

        statementBuilder.build(
                methodBuilder,
                methodInfo,
                "conn",
                connectionCall,
                STATEMENT_VAR,
                "rs",
                Collections.emptyList()
        );

        final var statementCommandVar = statementBuilder.getStatementCommandVar();
        final var executeCall = methodInfo.unParameterizedStatement() ?
                "$N.executeUpdate(" + statementCommandVar + ")" :
                "$N.executeUpdate()";

        if (methodInfo.isReturnRowsAffected()) {
            buildReturnRowsAffected(methodInfo, methodBuilder, executeCall);
        } else if (methodInfo.getReturnType().getKind() == TypeKind.VOID) {
            methodBuilder.addStatement(executeCall, STATEMENT_VAR);
        } else {
            buildReturnPK(methodInfo, methodBuilder, executeCall, methodInfo.getCustomReturnType());
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
