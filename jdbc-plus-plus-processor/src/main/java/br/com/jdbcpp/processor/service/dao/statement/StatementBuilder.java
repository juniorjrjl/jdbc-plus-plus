package br.com.jdbcpp.processor.service.dao.statement;

import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.method.SelectCollectionMethodInfo;
import br.com.jdbcpp.processor.dto.method.SelectNullableMethodInfo;
import br.com.jdbcpp.processor.dto.method.SelectOptionalMethodInfo;
import br.com.jdbcpp.processor.dto.method.customization.OperationCustomize;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import br.com.jdbcpp.processor.service.dao.statement.param.ClassParamResolver;
import br.com.jdbcpp.processor.service.dao.statement.param.SimpleParamResolver;
import br.com.jdbcpp.processor.service.dao.statement.param.StatementParamResolver;
import br.com.jdbcpp.processor.util.JDBCUtil;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

public class StatementBuilder {

    private static final String STATEMENT_COMMAND_VAR = "statement";

    private final UnparameterizedStatement unparameterizedStatement;
    private final PrepareStatement prepareStatement;

    public StatementBuilder(final UnparameterizedStatement unparameterizedStatement,
                            final PrepareStatement prepareStatement) {
        this.unparameterizedStatement = unparameterizedStatement;
        this.prepareStatement = prepareStatement;
    }

    public String getStatementCommandVar() {
        return STATEMENT_COMMAND_VAR;
    }

    public void build(final MethodSpec.Builder methodBuilder,
                      final MethodInfo methodInfo,
                      final String connectionVar,
                      final String connectionCall,
                      final String statementVar,
                      final String resultSetVar,
                      final List<String> dataNameOrIndex) {
        final var statement = methodInfo.getStatement();
        final var readMethod = methodInfo instanceof SelectNullableMethodInfo ||
                methodInfo instanceof SelectCollectionMethodInfo ||
                methodInfo instanceof SelectOptionalMethodInfo;
        if (methodInfo.unParameterizedStatement()){
            unparameterizedStatement.build(
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

        final StatementParamResolver statementResolver = methodInfo.getClassPropertyMap().isEmpty() ?
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

        prepareStatement.build(
                methodBuilder,
                statement,
                statementResolver,
                connectionVar,
                connectionCall,
                statementVar,
                dataNameOrIndex
        );

        final var operationCustomize = methodInfo.getOperationCustomize();
        if (isNull(operationCustomize) ||
                (operationCustomize.hasNoneInputMapRange() && isNull(operationCustomize.inputMap()))) {
            buildInputParams(methodBuilder, methodInfo, statement, statementResolver, statementVar);
            return;
        }

        if (nonNull(operationCustomize.inputMap())){
            buildInputParamFullCustom(methodBuilder, methodInfo.getParams(), operationCustomize, statementVar);
            return;
        }

        buildInputParamsWithCustomColumns(methodBuilder, methodInfo.getParams(), operationCustomize, statement, statementResolver, statementVar);

    }

    private void buildInputParamFullCustom(final MethodSpec.Builder methodBuilder,
                                           final List<ParamInfo> params,
                                           final OperationCustomize operationCustomize,
                                           final String statementVar){
        final var args = statementVar + ", " + params.stream().map(ParamInfo::getName)
                .collect(Collectors.joining(", "));
        methodBuilder.addStatement("$N($L)", operationCustomize.inputMap(), args);
    }

    private void buildInputParams(final MethodSpec.Builder methodBuilder,
                                  final MethodInfo methodInfo,
                                  final StatementInfo statementInfo,
                                  final StatementParamResolver statementResolver,
                                  final String statementVar){

        methodBuilder.addStatement("var paramIndex = 1");
        for(final var param: statementInfo.params()){
            final var leafParam = statementResolver.getParamInfo(param.name());
            final var path = statementResolver.resolveParamPath(param.name());

            if (isNull(leafParam.getContainerType())){
                final var stmtSetter = JDBCUtil.getPrepareStatementSetter(
                        path,
                        TypeName.get(leafParam.isCustomEnum() ?
                                requireNonNull(leafParam.getEnumMethodType()) :
                                leafParam.getType()),
                        statementVar,
                        "paramIndex++"
                );
                methodBuilder.addStatement(stmtSetter);
            } else {
                methodBuilder.beginControlFlow("for (final var x : $N)", path);
                final var stmtSetter = JDBCUtil.getPrepareStatementSetter(
                        "x",
                        TypeName.get(leafParam.isCustomEnum() ?
                                requireNonNull(leafParam.getEnumMethodType()) :
                                leafParam.getType()),
                        statementVar,
                        "paramIndex++"
                );
                methodBuilder.addStatement(stmtSetter);
                methodBuilder.endControlFlow();
            }
        }
    }

    private void buildInputParamsWithCustomColumns(final MethodSpec.Builder methodBuilder,
                                                   final List<ParamInfo> params,
                                                   final OperationCustomize operationCustomize,
                                                   final StatementInfo statementInfo,
                                                   final StatementParamResolver statementResolver,
                                                   final String statementVar){
        methodBuilder.addStatement("var paramIndex = 1");
        final var inputMapRange = operationCustomize.inputMapRange();
        var rangeIndex = 0;
        var currentParamPos = 1;
        final var sqlParams = statementInfo.params();

        for (int i = 0; i < sqlParams.size(); i++) {

            if (rangeIndex < inputMapRange.size() && currentParamPos == inputMapRange.get(rangeIndex).start()) {
                final var range = inputMapRange.get(rangeIndex);
                final var args = statementVar + ", " + params.stream().map(ParamInfo::getName)
                        .collect(Collectors.joining(", "));
                methodBuilder.addStatement("$L($L,$L)", range.method(), args, i + 1);

                var columnsCovered = (range.end() - range.start()) + 1;

                methodBuilder.addStatement("paramIndex += $L", columnsCovered);

                currentParamPos += columnsCovered;
                rangeIndex++;

                i += (columnsCovered - 1);
                continue;
            }

            final var param = sqlParams.get(i);
            final var leafParam = statementResolver.getParamInfo(param.name());
            final var path = statementResolver.resolveParamPath(param.name());

            if (isNull(leafParam.getContainerType())) {
                final var stmtSetter = JDBCUtil.getPrepareStatementSetter(
                        path,
                        TypeName.get(leafParam.isCustomEnum() ?
                                requireNonNull(leafParam.getEnumMethodType()) :
                                leafParam.getType()),
                        statementVar,
                        "paramIndex++"
                );
                methodBuilder.addStatement(stmtSetter);
                currentParamPos++;
            } else {
                methodBuilder.beginControlFlow("for (final var x : $N)", path);
                final var stmtSetter = JDBCUtil.getPrepareStatementSetter(
                        "x",
                        TypeName.get(leafParam.isCustomEnum() ?
                                requireNonNull(leafParam.getEnumMethodType()) :
                                leafParam.getType()),
                        statementVar,
                        "paramIndex++"
                );
                methodBuilder.addStatement(stmtSetter);
                methodBuilder.endControlFlow();
                currentParamPos++;
            }
        }
    }

}
