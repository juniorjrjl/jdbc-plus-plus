package br.com.jdbcpp.processor.service.dao.write;

import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.service.dao.MethodGenerator;
import br.com.jdbcpp.processor.service.dao.statement.StatementBuilder;
import br.com.jdbcpp.processor.util.JDBCUtil;
import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

public abstract class WriteMethodGenerator<T extends MethodInfo> implements MethodGenerator<T> {

    protected static final String STATEMENT_VAR = "stmt";

    protected final StatementBuilder statementBuilder;
    protected final TypeName sqlException;


    protected WriteMethodGenerator(final StatementBuilder statementBuilder,
                                   final TypeMirror sqlException){
        this.statementBuilder = statementBuilder;
        this.sqlException = TypeName.get(sqlException);
    }

    protected MethodSpec.Builder buildMethodSignature(final T methodInfo,
                                                      final TypeName receivedException){
        final var methodBuilder = MethodSpec.methodBuilder(methodInfo.getName())
                .addModifiers(PUBLIC)
                .returns(TypeName.get(methodInfo.getReturnType()));

        if (receivedException.equals(sqlException)){
            methodBuilder.addException(sqlException);
        }

        methodInfo.getAnnotations().stream()
                .map(AnnotationSpec::get)
                .forEach(methodBuilder::addAnnotation);

        methodInfo.getParams().forEach(p -> {
            final var paramBuilder = ParameterSpec.builder(
                    TypeName.get(p.getType()),
                    p.getName(),
                    FINAL
            );

            p.getAnnotations().stream()
                    .map(AnnotationSpec::get)
                    .forEach(paramBuilder::addAnnotation);

            methodBuilder.addParameter(paramBuilder.build());
        });

        return methodBuilder;
    }


    protected void buildReturnPK(final T methodInfo,
                                 final MethodSpec.Builder methodBuilder,
                                 final String executeCall,
                                 @Nullable
                                 final String customReturnType) {
        final var generatedPK = "generatedPK";
        final var generatedKeys = "generatedKeys";
        final var returnType = TypeName.get(methodInfo.getReturnType());
        final var customReturnTypeName = Optional.ofNullable(customReturnType)
                .map(ClassName::bestGuess)
                .orElse(null);
        methodBuilder.addStatement(executeCall, STATEMENT_VAR);
        methodBuilder.beginControlFlow("try (final var $N = $N.getGeneratedKeys())", generatedKeys, STATEMENT_VAR);
        methodBuilder.beginControlFlow("if ($N.next())", generatedKeys);

        final var operationCustomize = methodInfo.getOperationCustomize();
        if (nonNull(operationCustomize) && nonNull(operationCustomize.resultSetMap())) {
            methodBuilder.addStatement("return $N($N)", operationCustomize.resultSetMap(), generatedKeys);
        } else {
            JDBCUtil.getResultSetGetter(
                    isNull(customReturnTypeName) ? returnType : customReturnTypeName,
                    "1",
                    generatedKeys,
                    generatedPK,
                    false,
                    methodBuilder
            );
            methodBuilder.addStatement("return $N", generatedPK);
        }


        methodBuilder.nextControlFlow("else");
        methodBuilder.addStatement("throw new $T($S)", IllegalStateException.class, "Generated keys not found");
        methodBuilder.endControlFlow();
        methodBuilder.endControlFlow();
    }

    protected void buildReturnRowsAffected(final T methodInfo,
                                           final MethodSpec.Builder methodBuilder,
                                           final String executeCall) {
        if (TypeName.get(methodInfo.getReturnType()).isBoxedPrimitive() &&
                TypeName.get(methodInfo.getReturnType()).equals(ClassName.get(Long.class))) {
            methodBuilder.addStatement(
                    "return $T.valueOf(" + executeCall + ")",
                    Long.class,
                    STATEMENT_VAR
            );
        } else {
            methodBuilder.addStatement("return " + executeCall, STATEMENT_VAR);
        }
    }
}
