package br.com.jdbcpp.processor.service.statement;

import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import br.com.jdbcpp.processor.util.JDBCUtil;
import com.palantir.javapoet.ArrayTypeName;
import com.palantir.javapoet.MethodSpec;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

public class StatementBuilder {

    public StatementBuilder() {}

    public void build(final MethodSpec.Builder methodBuilder,
                      final MethodInfo methodInfo,
                      final String connectionCall) {
        final var statement = methodInfo.getStatement();
        if (methodInfo.useClassParam()){

        } else {
            if (methodInfo.getStatement().sqlNotSplit()) {
                if (methodInfo.getParams().isEmpty()){
                    statementBuild(methodBuilder, statement.getNoSplitFullSQL(), connectionCall);
                } else {
                    prepareStatementSimpleParamBuild(
                            methodBuilder,
                            statement,
                            methodInfo.getSimpleParams(),
                            methodInfo.getName(),
                            connectionCall
                    );
                }
            } else {
                prepareStatementSimpleParamWithListBuild(
                        methodBuilder,
                        statement,
                        methodInfo.getSimpleParams(),
                        methodInfo.getName(),
                        connectionCall
                );
            }
        }
    }

    private void statementBuild(final MethodSpec.Builder methodBuilder,
                                final String statement,
                                final String getConnectionCall){
        methodBuilder.addStatement("final var statement = $S", statement)
                .beginControlFlow("""
                        try(final var connection = $S;
                        final var stmt = connection.createStatement())
                        """, getConnectionCall);
    }

    private void prepareStatementSimpleParamBuild(final MethodSpec.Builder methodBuilder,
                                                  final StatementInfo statement,
                                                  final List<SimpleParamInfo> simpleParams,
                                                  final String methodName,
                                                  final String getConnectionCall){
        methodBuilder.addStatement("final var statement = $S", statement.getNoSplitFullSQL())
                .beginControlFlow("""
                    try (final var connection = $N;
                    final var pstmt = conn.prepareStatement(sql))
                    """, getConnectionCall)
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
                    "pstmt",
                    "paramIndex++"
            );
            methodBuilder.addStatement(stmtSetter);
        }
    }

    private void prepareStatementSimpleParamWithListBuild(final MethodSpec.Builder methodBuilder,
                                                          final StatementInfo statement,
                                                          final List<SimpleParamInfo> simpleParams,
                                                          final String methodName,
                                                          final String getConnectionCall){
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
        for (final var amount : paramsAmountName){
            methodBuilder.addStatement("preStatement.append($S)", "(?");
            methodBuilder.beginControlFlow("if ($N > 1)", amount);
            methodBuilder.addStatement("preStatement.append($S.repeat($N - 1))", ", ?", amount);
            methodBuilder.endControlFlow();
            methodBuilder.addStatement("preStatement.append($S)", ")");
            methodBuilder.addStatement("preStatement.append($S)", sqlStatement.removeFirst());
        }
        methodBuilder.addStatement("preStatement.append($S)", sqlStatement.removeFirst());
        methodBuilder.addStatement("final var statement = preStatement.toString()")
                .beginControlFlow("""
                    try (final var connection = $N;
                    final var pstmt = conn.prepareStatement(sql))
                    """, getConnectionCall)
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
                        "pstmt",
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
                        "pstmt",
                        "paramIndex++");
                methodBuilder.addStatement(stmtSetter);
                methodBuilder.endControlFlow();
            }
        }
    }

}
