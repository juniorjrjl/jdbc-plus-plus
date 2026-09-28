package br.com.jdbcpp.processor.service.dao.read.select.result;

import br.com.jdbcpp.processor.dto.method.customization.ColumnMapRangeInfo;
import br.com.jdbcpp.processor.dto.method.customization.InputMapRangeInfo;
import br.com.jdbcpp.processor.dto.method.customization.OperationCustomize;
import br.com.jdbcpp.processor.dto.result.SetterStrategy;
import br.com.jdbcpp.processor.util.JDBCUtil;
import br.com.jdbcpp.processor.util.StringUtil;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;

public class SelectResultUsingSetter {

    public void build(final List<SetterStrategy> strategies,
                      final String objectResultName,
                      final TypeMirror returnType,
                      final String resultSetVar,
                      final MethodSpec.Builder builder,
                      @Nullable
                      final OperationCustomize operationCustomize) {
        builder.addStatement("final var $L = new $T()", objectResultName, TypeName.get(returnType));
        if (isNull(operationCustomize) || operationCustomize.inputMapRange().isEmpty()){
            withoutCustomization(strategies, objectResultName, resultSetVar, builder);
        } else {
            withPartialCustomization(strategies, objectResultName, resultSetVar, builder, operationCustomize.columnMapRange());
        }
    }

    private void withoutCustomization(final List<SetterStrategy> strategies,
                                      final String objectResultName,
                                      final String resultSetVar,
                                      final MethodSpec.Builder builder){
        for (final var strategy : strategies) {
            final var rsValue = JDBCUtil.getResultSetGetter(
                    TypeName.get(strategy.getType()),
                    Optional.ofNullable(strategy.getResultSetIndex())
                            .map(String::valueOf)
                            .orElseGet(() ->{
                                final var columnName = StringUtil.camelToSnakeCase(strategy.getName());
                                return StringUtil.toQuotedString(columnName);
                            }),
                    resultSetVar,
                    strategy.getName(),
                    true,
                    builder);
            builder.addStatement("$L.$N($L)", objectResultName, strategy.getMethodName(), rsValue);
        }
    }

    private void withPartialCustomization(final List<SetterStrategy> strategies,
                                          final String objectResultName,
                                          final String resultSetVar,
                                          final MethodSpec.Builder builder,
                                          final List<ColumnMapRangeInfo> columnMapRange){
        var rangeIndex = 0;
        var currentParamPos = 1;
        var i = 0;
        while (i < strategies.size()) {

            final var strategy = strategies.get(i);

            if (rangeIndex < columnMapRange.size() && currentParamPos == columnMapRange.get(rangeIndex).start()) {
                final var varName = strategy.getName();
                final var rsVarName = "rs"+ varName.substring(0, 1).toUpperCase() + varName.substring(1);
                final var range = columnMapRange.get(rangeIndex);
                builder.addStatement("final var $L = $L($L, $L)", rsVarName, range.method(), resultSetVar, i + 1);

                var columnsCovered = (range.end() - range.start()) + 1;

                builder.addStatement("paramIndex += $L", columnsCovered);
                currentParamPos += columnsCovered;
                rangeIndex++;

                i += (columnsCovered - 1);
                continue;
            }

            final var rsValue = JDBCUtil.getResultSetGetter(
                    TypeName.get(strategy.getType()),
                    Optional.ofNullable(strategy.getResultSetIndex())
                            .map(String::valueOf)
                            .orElseGet(() ->{
                                final var columnName = StringUtil.camelToSnakeCase(strategy.getName());
                                return StringUtil.toQuotedString(columnName);
                            }),
                    resultSetVar,
                    strategy.getName(),
                    true,
                    builder);
            builder.addStatement("$L.$N($L)", objectResultName, strategy.getMethodName(), rsValue);
            i++;
        }
    }

}
