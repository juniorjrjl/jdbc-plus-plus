package br.com.jdbcpp.processor.service.dao.write.insert;

import br.com.jdbcpp.processor.dto.method.InsertMethod;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.service.dao.statement.StatementBuilder;
import br.com.jdbcpp.processor.service.dao.write.WriteMethodGenerator;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class InsertMethodGenerator extends WriteMethodGenerator<InsertMethod> {

    public InsertMethodGenerator(final StatementBuilder statementBuilder,
                                 final TypeMirror sqlException){
        super(statementBuilder, sqlException);
    }

    @Override
    public boolean useInstance(final MethodInfo methodInfo) {
        return methodInfo instanceof InsertMethod;
    }

    @Override
    public MethodSpec.Builder build(final InsertMethod methodInfo,
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
                methodInfo.getDataNameOrIndex()
        );
        final var statementCommandVar = statementBuilder.getStatementCommandVar();
        final String executeCall = methodInfo.unParameterizedStatement()
                ? "$N.executeUpdate(" + statementCommandVar + ")"
                : "$N.executeUpdate()";

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
