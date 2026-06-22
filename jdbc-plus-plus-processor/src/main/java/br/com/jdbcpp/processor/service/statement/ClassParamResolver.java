package br.com.jdbcpp.processor.service.statement;

import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfo;
import com.palantir.javapoet.ArrayTypeName;
import com.palantir.javapoet.MethodSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClassParamResolver implements StatementResolver{

    private final String methodName;
    private final Map<String, List<ParamInfo>> classPropertyMap;
    private final List<ParamInfo> collectionParams;

    public ClassParamResolver(final String methodName,
                              final Map<String, List<ParamInfo>> classPropertyMap,
                              final List<ParamInfo> collectionParams) {
        this.methodName = methodName;
        this.classPropertyMap = classPropertyMap;
        this.collectionParams = collectionParams;
    }

    @Override
    public String resolveParamPath(final String queryParamName) {
        final var selectedParam = Optional.ofNullable(classPropertyMap.get(queryParamName))
                .orElseThrow(() -> {
                    final var message = String.format(
                            "Param %s not found in method %s",
                            queryParamName,
                            methodName
                    );
                    return new IllegalArgumentException(message);
                });
        final var root = selectedParam.getFirst().getConvertMethod();
        return root + "." + selectedParam.stream()
                .skip(1)
                .map(p -> p.getConvertMethod() + "()")
                .collect(Collectors.joining("."));
    }

    @Override
    public SimpleParamInfo getParamInfo(final String queryParamName) {
        return (SimpleParamInfo)classPropertyMap.get(queryParamName).getLast();
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
