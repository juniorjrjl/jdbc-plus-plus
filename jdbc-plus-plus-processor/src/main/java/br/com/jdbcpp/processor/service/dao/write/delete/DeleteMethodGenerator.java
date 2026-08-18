package br.com.jdbcpp.processor.service.dao.write.delete;

import br.com.jdbcpp.processor.dto.method.DeleteMethod;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.service.dao.MethodGenerator;
import br.com.jdbcpp.processor.service.dao.statement.StatementBuilder;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

public class DeleteMethodGenerator implements MethodGenerator<DeleteMethod> {

    private final StatementBuilder statementBuilder;
    private final TypeName sqlException;

    public DeleteMethodGenerator(final StatementBuilder statementBuilder,
                                 final TypeMirror sqlException){
        this.statementBuilder = statementBuilder;
        this.sqlException = TypeName.get(sqlException);
    }

    @Override
    public boolean useInstance(final MethodInfo methodInfo) {
        return methodInfo instanceof DeleteMethod;
    }

    @Override
    public MethodSpec.Builder build(final DeleteMethod methodInfo,
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
                null
        );
        final var statementCommandVar = statementBuilder.getStatementCommandVar();
        final var executeCall = methodInfo.unParameterizedStatement() ?
                "$N.executeUpdate(" + statementCommandVar + ")" :
                "$N.executeUpdate()";

        if (methodInfo.isReturnRowsAffected()){
            if (TypeName.get(methodInfo.getReturnType()).isBoxedPrimitive() &&
                    TypeName.get(methodInfo.getReturnType()).equals(ClassName.get(Long.class))){
                methodBuilder.addStatement("return $T.valueOf(" + executeCall + ")", Long.class, statementVar);
            } else {
                methodBuilder.addStatement("return " + executeCall, statementVar);
            }
        } else {
            methodBuilder.addStatement(executeCall, statementVar);
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
