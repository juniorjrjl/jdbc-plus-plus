package br.com.jdbcpp.processor.service.dao.statement;

import com.palantir.javapoet.MethodSpec;

public class UnparameterizedStatement {

    private static final String STATEMENT_COMMAND_VAR = "statement";

    public void build(final MethodSpec.Builder methodBuilder,
                      final String statement,
                      final String connectionVar,
                      final String connectionCall,
                      final String statementVar,
                      final String resultSetVar,
                      final boolean readMethod){
        if (statement.contains("\n")){
            methodBuilder.addStatement("final var $N = $L", STATEMENT_COMMAND_VAR, "\"\"\"\n" + statement + "\"\"\"");
        } else {
            methodBuilder.addStatement("final var $N = $S", STATEMENT_COMMAND_VAR, statement);
        }

        if (readMethod) {
            final var tryExecuteStatementVar = """
                        try(final var $N = $N;
                        final var $N = $N.createStatement();
                        final var $N = $N.executeQuery(statement))
                        """;
            methodBuilder.beginControlFlow(
                    tryExecuteStatementVar,
                    connectionVar,
                    connectionCall,
                    statementVar,
                    connectionVar,
                    resultSetVar,
                    statementVar
            );
            return;
        }
        final var tryExecuteStatementVar = """
                        try(final var $N = $N;
                        final var $N = $N.createStatement())
                        """;
        methodBuilder.beginControlFlow(
                tryExecuteStatementVar,
                connectionVar,
                connectionCall,
                statementVar,
                connectionVar
        );
    }

}
