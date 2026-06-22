package br.com.jdbcpp.processor.service.statement;

import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfo;
import com.palantir.javapoet.ArrayTypeName;
import com.palantir.javapoet.MethodSpec;

import java.util.ArrayList;
import java.util.List;

public class SimpleParamResolver implements StatementResolver{

    private final String methodName;
    private final List<SimpleParamInfo> params;
    private final List<ParamInfo> collectionParams;

    public SimpleParamResolver(final String methodName,
                               final List<SimpleParamInfo> params,
                               final List<ParamInfo> collectionParams) {
        this.methodName = methodName;
        this.params = params;
        this.collectionParams = collectionParams;
    }

    @Override
    public String resolveParamPath(final String queryParamName) {
        return getParamInfo(queryParamName).getName();
    }

    @Override
    public SimpleParamInfo getParamInfo(final String queryParamName) {
        return params.stream()
                .filter(p -> p.getQueryParamName().equals(queryParamName))
                .findFirst()
                .orElseThrow(() -> {
                    final var message = String.format(
                            "Param %s not found in method %s",
                            queryParamName,
                            methodName
                    );
                    return new IllegalArgumentException(message);
                });
    }

    @Override
    public List<String> buildCollectionSizes(final MethodSpec.Builder methodBuilder,
                                             final List<String> sql) {
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
        final var sqlStatement = new ArrayList<>(sql);
        methodBuilder.addStatement(
                "final var preStatement = new $T($S)",
                StringBuilder.class,
                sqlStatement.removeFirst()
        );
        final var preStatementAppend = "preStatement.append($S)";
        for (final var amount : paramsAmountName){
            methodBuilder.addStatement(preStatementAppend, "(?");
            methodBuilder.beginControlFlow("if ($N > 1)", amount);
            methodBuilder.addStatement("preStatement.append($S.repeat($N - 1))", ", ?", amount);
            methodBuilder.endControlFlow();
            methodBuilder.addStatement(preStatementAppend, ")");
            methodBuilder.addStatement(preStatementAppend, sqlStatement.removeFirst());
        }
        methodBuilder.addStatement(preStatementAppend, sqlStatement.removeFirst());
        return paramsAmountName;
    }

}
