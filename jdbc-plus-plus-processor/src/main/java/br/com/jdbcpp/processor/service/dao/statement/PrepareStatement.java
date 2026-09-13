package br.com.jdbcpp.processor.service.dao.statement;

import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import br.com.jdbcpp.processor.service.dao.statement.param.StatementParamResolver;
import com.palantir.javapoet.MethodSpec;

import java.util.List;

public class PrepareStatement {

    private static final String STATEMENT_COMMAND_VAR = "statement";

    public void build(final MethodSpec.Builder methodBuilder,
                      final StatementInfo statementInfo,
                      final StatementParamResolver statementResolver,
                      final String connectionVar,
                      final String connectionCall,
                      final String statementVar,
                      final List<String> dataNameOrIndex){
        if (statementInfo.sqlNotSplit()){

            if (statementInfo.getNoSplitFullSQL().contains("\n")){methodBuilder.addStatement(
                    "final var $N = $L",
                    STATEMENT_COMMAND_VAR,
                    "\"\"\"\n" + statementInfo.getNoSplitFullSQL() + "\"\"\""
            );
            } else {
                methodBuilder.addStatement(
                        "final var $N = $S",
                        STATEMENT_COMMAND_VAR,
                        statementInfo.getNoSplitFullSQL()
                );
            }
        } else {
            statementResolver.buildCollectionSizes(methodBuilder, statementInfo.sql());
            methodBuilder.addStatement("final var $N = preStatement.toString()", STATEMENT_COMMAND_VAR);
        }

        if (!dataNameOrIndex.isEmpty()){
            final var tryPrepareStmt = dataNameOrIndex.stream()
                    .allMatch(d -> d.chars().allMatch(Character::isDigit)) ?
                    """
                    try (final var $N = $N;
                    final var $N = $N.prepareStatement($N, new int[] { $L }))
                    """ :
                    """
                    try (final var $N = $N;
                    final var $N = $N.prepareStatement($N, new String[] { $S }))
                    """;
            methodBuilder.beginControlFlow(tryPrepareStmt,
                    connectionVar,
                    connectionCall,
                    statementVar,
                    connectionVar,
                    STATEMENT_COMMAND_VAR,
                    String.join(", ", dataNameOrIndex)
            );
        } else {
            methodBuilder.beginControlFlow("""
                    try (final var $N = $N;
                    final var $N = $N.prepareStatement($N))
                    """,
                    connectionVar,
                    connectionCall,
                    statementVar,
                    connectionVar,
                    STATEMENT_COMMAND_VAR
            );
        }
    }

}
