package br.com.jdbcpp.processor.service.statement;

import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import br.com.jdbcpp.processor.util.JDBCUtil;
import com.palantir.javapoet.MethodSpec;

import java.util.List;

import static java.util.Objects.isNull;

public class StatementBuilder {

    private static final String STATEMENT_COMMAND_VAR = "statement";

    public String getStatementCommandVar() {
        return STATEMENT_COMMAND_VAR;
    }

    public void build(final MethodSpec.Builder methodBuilder,
                      final MethodInfo methodInfo,
                      final String connectionVar,
                      final String connectionCall,
                      final String statementVar,
                      final String resultSetVar) {
        final var statement = methodInfo.getStatement();
        final var readMethod = methodInfo instanceof SelectMethodInfo;
        if (methodInfo.unParameterizedStatement()){
            buildStatement(
                    methodBuilder,
                    statement.getNoSplitFullSQL(),
                    connectionVar,
                    connectionCall,
                    statementVar,
                    resultSetVar,
                    readMethod
            );
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

        buildPreparedStatement(
                methodBuilder,
                statement,
                statementResolver,
                connectionVar,
                connectionCall,
                statementVar
        );
    }

    private void buildStatement(final MethodSpec.Builder methodBuilder,
                                final String statement,
                                final String connectionVar,
                                final String connectionCall,
                                final String statementVar,
                                final String resultSetVar,
                                final boolean readMethod){
        methodBuilder.addStatement("final var $N = $S", STATEMENT_COMMAND_VAR, statement);
        if (readMethod) {
            methodBuilder.beginControlFlow("""
                        try(final var $N = $N;
                        final var $N = $N.createStatement();
                        final var $N = $N.executeQuery(statement))
                        """,
                    connectionVar,
                    connectionCall,
                    statementVar,
                    connectionVar,
                    resultSetVar,
                    statementVar
            );
            return;
        }
        methodBuilder.beginControlFlow("""
                        try(final var $N = $N;
                        final var $N = $N.createStatement())
                        """,
                connectionVar,
                connectionCall,
                statementVar,
                connectionVar);
    }

    private void buildPreparedStatement(final MethodSpec.Builder methodBuilder,
                                        final StatementInfo statementInfo,
                                        final StatementResolver statementResolver,
                                        final String connectionVar,
                                        final String connectionCall,
                                        final String statementVar){
        if (statementInfo.sqlNotSplit()){
            methodBuilder.addStatement(
                    "final var $N = $S",
                    STATEMENT_COMMAND_VAR,
                    statementInfo.getNoSplitFullSQL()
            );
        } else {
            statementResolver.buildCollectionSizes(methodBuilder, statementInfo.sql());
            methodBuilder.addStatement("final var $N = preStatement.toString()", STATEMENT_COMMAND_VAR);
        }
        methodBuilder.beginControlFlow("""
                    try (final var $N = $N;
                    final var $N = $N.prepareStatement($N))
                    """,
                        connectionVar,
                        connectionCall,
                        statementVar,
                        connectionVar,
                        STATEMENT_COMMAND_VAR
                )
                .addStatement("var paramIndex = 1");
        for(final var param: statementInfo.params()){
            final var leafParam = statementResolver.getParamInfo(param.name());
            final var path = statementResolver.resolveParamPath(param.name());

            if (isNull(leafParam.getContainerType())){
                final var stmtSetter = JDBCUtil.getPrepareStatementSetter(
                        path,
                        leafParam.getType(),
                        leafParam.getConvertMethod(),
                        leafParam.isCustomEnum(),
                        statementVar,
                        "paramIndex++"
                );
                methodBuilder.addStatement(stmtSetter);
            } else {
                methodBuilder.beginControlFlow("for (final var x : $N)", path);
                final var stmtSetter = JDBCUtil.getPrepareStatementSetter(
                        "x",
                        leafParam.getType(),
                        leafParam.getConvertMethod(),
                        leafParam.isCustomEnum(),
                        statementVar,
                        "paramIndex++"
                );
                methodBuilder.addStatement(stmtSetter);
                methodBuilder.endControlFlow();
            }
        }
    }

}
