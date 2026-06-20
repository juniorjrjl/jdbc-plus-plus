package br.com.jdbcpp.processor.service.write;

import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import br.com.jdbcpp.processor.util.JDBCUtil;
import com.palantir.javapoet.ArrayTypeName;
import com.palantir.javapoet.MethodSpec;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static javax.lang.model.element.Modifier.PUBLIC;

public abstract class WriteSQLStatementMethod<T extends MethodInfo> {


    public MethodSpec.Builder build(final T methodInfo, final String connectionCall){
        final var methodBuilder = MethodSpec.methodBuilder(methodInfo.getName())
                .addModifiers(PUBLIC)
                .returns(methodInfo.getReturnType());
        final var statement = methodInfo.getStatement();
        final String statementVar;
        if (methodInfo.useClassParam()){
            statementVar = "";
        } else {
            if (methodInfo.getStatement().sqlNotSplit()) {
                if (methodInfo.getParams().isEmpty()){
                    statementVar = statementBuild(methodBuilder, statement.getNoSplitFullSQL(), connectionCall);
                } else {
                    statementVar = prepareStatementSimpleParamBuild(
                            methodBuilder,
                            statement,
                            methodInfo.getSimpleParams(),
                            methodInfo.getName(),
                            connectionCall
                    );
                }
            } else {
                statementVar = prepareStatementSimpleParamWithListBuild(
                        methodBuilder,
                        statement,
                        methodInfo.getSimpleParams(),
                        methodInfo.getName(),
                        connectionCall
                );
            }
        }
        return buildBaseBody(methodInfo, methodBuilder, statementVar);
    }


    protected abstract MethodSpec.Builder buildBaseBody(final T methodInfo,
                                                        final MethodSpec.Builder methodBuilder,
                                                        final String statementVar);

    private String statementBuild(final MethodSpec.Builder methodBuilder,
                                  final String statement,
                                  final String connectionCall){
        final var statementVar = "stmt";
        methodBuilder.addStatement("final var statement = $S", statement)
                .beginControlFlow("""
                        try(final var conn = $N;
                        final var $N = conn.createStatement())
                        """, statementVar, connectionCall);
        return statementVar;
    }

    private String prepareStatementSimpleParamBuild(final MethodSpec.Builder methodBuilder,
                                                    final StatementInfo statement,
                                                    final List<SimpleParamInfo> simpleParams,
                                                    final String methodName,
                                                    final String connectionCall){
        final var prepareStatementVar = "pstmt";
        methodBuilder.addStatement("final var statement = $S", statement.getNoSplitFullSQL())
                .beginControlFlow("""
                        try(final var conn = $N;
                        final var $N = conn.prepareStatement(statement))
                        """, prepareStatementVar, connectionCall)
                .addStatement("var paramIndex = 1");
        for (final var param : statement.params()){
            final var inputParam = simpleParams.stream()
                    .filter(p -> p.getQueryParamName().equals(param.name()))
                    .findFirst()
                    .orElseThrow(() -> {
                        final var message = String.format(
                                "Param %s not found in method %s",
                                param.name(),
                                methodName
                        );
                        return new IllegalArgumentException(message);
                    });
            final var stmtSetter = JDBCUtil.getPrepareStatementSetter(
                    inputParam.getName(),
                    inputParam.getType(),
                    inputParam.getConvertMethod(),
                    inputParam.isCustomEnum(),
                    prepareStatementVar,
                    "paramIndex++"
            );
            methodBuilder.addStatement(stmtSetter);
        }
        return prepareStatementVar;
    }

    private String prepareStatementSimpleParamWithListBuild(final MethodSpec.Builder methodBuilder,
                                                            final StatementInfo statement,
                                                            final List<SimpleParamInfo> simpleParams,
                                                            final String methodName,
                                                            final String connectionCall){
        final var prepareStatementVar = "pstmt";
        final var collectionParams = simpleParams.stream()
                .filter(SimpleParamInfo::hasContainer)
                .toList();
        final List<String> paramsAmountName = new ArrayList<>();
        for (final var listParam : collectionParams){
            final var paramAmountName = listParam.getName() + "size";
            if (listParam.getContainerType() instanceof ArrayTypeName){
                methodBuilder.addStatement("final var $N = $N.length", paramAmountName, listParam.getName());
            } else {
                methodBuilder.addStatement("final var $N = $N.size()", paramAmountName, listParam.getName());
            }
            paramsAmountName.add(paramAmountName);
        }
        final var sqlStatement = new ArrayList<>(statement.sql());
        methodBuilder.addStatement(
                "final var preStatement = new $T($S)",
                StringBuilder.class,
                sqlStatement.removeFirst()
        );
        final var stmtBuilder = "preStatement.append($S)";
        for (final var amount : paramsAmountName){
            methodBuilder.addStatement(stmtBuilder, "(?");
            methodBuilder.beginControlFlow("if ($N > 1)", amount);
            methodBuilder.addStatement("preStatement.append($S.repeat($N - 1))", ", ?", amount);
            methodBuilder.endControlFlow();
            methodBuilder.addStatement(stmtBuilder, ")");
            methodBuilder.addStatement(stmtBuilder, sqlStatement.removeFirst());
        }
        methodBuilder.addStatement(stmtBuilder, sqlStatement.removeFirst());

        methodBuilder.addStatement("final var statement = preStatement.toString()")
                .beginControlFlow("""
                        try(final var conn = $N;
                        final var $N = conn.prepareStatement(statement))
                        """, prepareStatementVar, connectionCall)
                .addStatement("var paramIndex = 1");
        for (final var param : statement.params()){
            final var inputParam = simpleParams.stream()
                    .filter(p -> p.getQueryParamName().equals(param.name()))
                    .findFirst()
                    .orElseThrow(() -> {
                        final var message = String.format(
                                "Param %s not found in method %s",
                                param.name(),
                                methodName
                        );
                        return new IllegalArgumentException(message);
                    });
            final String stmtSetter;
            if (isNull(inputParam.getContainerType())) {
                stmtSetter = JDBCUtil.getPrepareStatementSetter(
                        inputParam.getName(),
                        inputParam.getType(),
                        inputParam.getConvertMethod(),
                        inputParam.isCustomEnum(),
                        prepareStatementVar,
                        "paramIndex++");
                methodBuilder.addStatement(stmtSetter);
            } else {
                methodBuilder.beginControlFlow(
                        "for (final var x : $N)",
                        inputParam.getName()
                );
                stmtSetter = JDBCUtil.getPrepareStatementSetter(
                        inputParam.getName(),
                        inputParam.getType(),
                        inputParam.getConvertMethod(),
                        inputParam.isCustomEnum(),
                        prepareStatementVar,
                        "paramIndex++");
                methodBuilder.addStatement(stmtSetter);
                methodBuilder.endControlFlow();
            }
        }
        return prepareStatementVar;
    }

}
