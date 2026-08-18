package br.com.jdbcpp.processor.service.dao.write.insert;

import br.com.jdbcpp.processor.dto.method.InsertMethod;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.service.dao.MethodGenerator;
import br.com.jdbcpp.processor.service.dao.statement.StatementBuilder;
import br.com.jdbcpp.processor.util.JDBCUtil;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

public class InsertMethodGenerator implements MethodGenerator<InsertMethod> {

    private final StatementBuilder statementBuilder;
    private final TypeName sqlException;


    public InsertMethodGenerator(final StatementBuilder statementBuilder,
                                 final TypeMirror sqlException){
        this.statementBuilder = statementBuilder;
        this.sqlException = TypeName.get(sqlException);
    }

    @Override
    public boolean useInstance(final MethodInfo methodInfo) {
        return methodInfo instanceof InsertMethod;
    }

    @Override
    public MethodSpec.Builder build(final InsertMethod methodInfo,
                                    final String connectionCall) {
        final var methodBuilder = MethodSpec.methodBuilder(methodInfo.getName())
                .addModifiers(PUBLIC)
                .returns(TypeName.get(methodInfo.getReturnType()));

        final var receivedException = TypeName.get(methodInfo.getPackException());
        if (receivedException.equals(sqlException)){
            methodBuilder.addException(sqlException);
        }

        methodInfo.getParams().forEach(p -> methodBuilder.addParameter(TypeName.get(p.getType()), p.getName(), FINAL));

        final var statementVar = "stmt";
        statementBuilder.build(
                methodBuilder,
                methodInfo,
                "conn",
                connectionCall,
                statementVar,
                "rs",
                methodInfo.getPkNameOrIndex()
        );
        final var statementCommandVar = statementBuilder.getStatementCommandVar();
        final String executeCall = methodInfo.unParameterizedStatement()
                ? "$N.executeUpdate(" + statementCommandVar + ")"
                : "$N.executeUpdate()";

        if (methodInfo.isReturnRowsAffected()) {
            if (TypeName.get(methodInfo.getReturnType()).isBoxedPrimitive() &&
                    TypeName.get(methodInfo.getReturnType()).equals(ClassName.get(Long.class))){
                methodBuilder.addStatement(
                        "return $T.valueOf(" + executeCall + ")",
                        Long.class,
                        statementVar
                );
            } else {
                methodBuilder.addStatement("return " + executeCall, statementVar);
            }
        } else if (methodInfo.getReturnType().getKind() == TypeKind.VOID) {
            methodBuilder.addStatement(executeCall, statementVar);
        } else {
            final var generatedPK = "generatedPK";
            final var generatedKeys = "generatedKeys";
            final var returnType = TypeName.get(methodInfo.getReturnType());
            methodBuilder.addStatement(executeCall, statementVar);
            methodBuilder.beginControlFlow("try (final var $N = $N.getGeneratedKeys())", generatedKeys, statementVar);
            methodBuilder.beginControlFlow("if ($N.next())", generatedKeys);
            JDBCUtil.getResultSetGetter(
                    returnType,
                    "1",
                    generatedKeys,
                    generatedPK,
                    false,
                    methodBuilder
                    );
            methodBuilder.addStatement("return $N", generatedPK);
            methodBuilder.nextControlFlow("else");
            methodBuilder.addStatement("throw new $T($S)", IllegalStateException.class, "Generated keys not found");
            methodBuilder.endControlFlow();
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
