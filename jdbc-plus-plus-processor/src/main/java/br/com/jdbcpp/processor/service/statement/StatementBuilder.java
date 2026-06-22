package br.com.jdbcpp.processor.service.statement;

import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import br.com.jdbcpp.processor.util.JDBCUtil;
import com.palantir.javapoet.MethodSpec;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static java.util.Objects.isNull;

public class StatementBuilder {

    public void build(final MethodSpec.Builder methodBuilder,
                      final MethodInfo methodInfo,
                      final String connectionCall) {
        final var statement = methodInfo.getStatement();
        final var readMethod = methodInfo instanceof SelectMethodInfo;
        if (methodInfo.unParameterizedStatement()){
            buildStatement(methodBuilder, statement.getNoSplitFullSQL(), connectionCall, readMethod);
            return;
        }

        final StatementResolver statementResolver = methodInfo.getClassPropertyMap().isEmpty() ?
                new SimpleParamResolver(
                        methodInfo.getName(),
                        methodInfo.getSimpleParams(),
                        methodInfo.getParams().stream()
                                .filter(ParamInfo::hasContainer)
                                .toList()
                ) :
                new ClassParamResolver(
                        methodInfo.getName(),
                        methodInfo.getClassPropertyMap(),
                        methodInfo.getClassPropertyMap().values().stream()
                                .map(List::getLast)
                                .filter(ParamInfo::hasContainer)
                                .toList()
                );

        buildPreparedStatement(methodBuilder, statement, statementResolver, connectionCall, readMethod);
    }

    @Nullable
    private String buildStatement(final MethodSpec.Builder methodBuilder,
                                  final String statement,
                                  final String connectionCall,
                                  final boolean readMethod){
        methodBuilder.addStatement("final var statement = $S", statement);
        if (readMethod) {
            final var statementVar = "stmt";
            methodBuilder.beginControlFlow("""
                        try(final var conn = $N;
                        final var $N = conn.createStatement();
                        final var rs = stmt.executeQuery(statement))
                        """, statementVar, connectionCall);
            return statementVar;
        }
        methodBuilder.beginControlFlow("""
                        try(final var conn = $L;
                        final var stmt = conn.createStatement())
                        """, connectionCall);
        return null;
    }

    private void buildPreparedStatement(final MethodSpec.Builder methodBuilder,
                                        final StatementInfo statementInfo,
                                        final StatementResolver statementResolver,
                                        final String connectionCall,
                                        final boolean readMethod){
        if (statementInfo.sqlNotSplit()){
            methodBuilder.addStatement("final var statement = $S", statementInfo.getNoSplitFullSQL());
        } else {
            statementResolver.buildCollectionSizes(methodBuilder, statementInfo.sql());
            methodBuilder.addStatement("final var statement = preStatement.toString()");
        }
        methodBuilder.beginControlFlow("""
                    try (final var conn = $L;
                    final var pstmt = conn.prepareStatement(statement))
                    """, connectionCall)
                .addStatement("var paramIndex = 1");
        for(final var param: statementInfo.params()){
            final var leafParam = statementResolver.getParamInfo(param.name());
            final var path = statementResolver.resolveParamPath(param.name());

            if (isNull(leafParam.getContainerType())){
                final var stmtSetter = JDBCUtil.getPrepareStatementSetter(
                        path,
                        leafParam.getType(),
                        leafParam.getConvertMethod(),
                        ((SimpleParamInfo) leafParam).isCustomEnum(),
                        "pstmt",
                        "paramIndex++"
                );
                methodBuilder.addStatement(stmtSetter);
            } else {
                methodBuilder.beginControlFlow("for (final var x : $N)", path);
                final var stmtSetter = JDBCUtil.getPrepareStatementSetter(
                        "x",
                        leafParam.getType(),
                        leafParam.getConvertMethod(),
                        ((SimpleParamInfo) leafParam).isCustomEnum(),
                        "pstmt",
                        "paramIndex++"
                );
                methodBuilder.addStatement(stmtSetter);
                methodBuilder.endControlFlow();
            }
        }
    }

}
