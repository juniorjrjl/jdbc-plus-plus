package br.com.jdbcpp.processor.service.dao.read.select.result;

import br.com.jdbcpp.processor.dto.method.customization.ColumnMapRangeInfo;
import br.com.jdbcpp.processor.dto.method.customization.InputMapRangeInfo;
import br.com.jdbcpp.processor.dto.method.customization.OperationCustomize;
import br.com.jdbcpp.processor.dto.result.ConstructorStrategy;
import br.com.jdbcpp.processor.util.JDBCUtil;
import br.com.jdbcpp.processor.util.StringUtil;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;

public class SelectResultUsingConstructor {

    public void build(final List<ConstructorStrategy> strategies,
                      final String objectResultName,
                      final TypeMirror returnType,
                      final String resultSetVar,
                      final MethodSpec.Builder builder,
                      @Nullable
                      final OperationCustomize operationCustomize) {
        final var constructorCode = new StringBuilder("final var $L = new $T(");
        if (isNull(operationCustomize) || operationCustomize.columnMapRange().isEmpty()) {
            withoutCustomization(strategies, objectResultName, returnType, resultSetVar, builder, constructorCode);
        } else {
            withPartialCustomization(strategies, objectResultName, returnType, resultSetVar, builder, constructorCode, operationCustomize.columnMapRange());
        }
    }

    private static void withoutCustomization(final List<ConstructorStrategy> strategies,
                                             final String objectResultName,
                                             final TypeMirror returnType,
                                             final String resultSetVar,
                                             final MethodSpec.Builder builder,
                                             final StringBuilder constructorCode) {
        final var rsVarNames = new String[strategies.size()];
        for (int i = 0; i < strategies.size(); i++) {
            final var strategy = strategies.get(i);
            final var rsVarName = JDBCUtil.getResultSetGetter(
                    TypeName.get(strategy.getType()),
                    Optional.ofNullable(strategy.getResultSetIndex())
                            .map(String::valueOf)
                            .orElseGet(() -> {
                                final var columnName = StringUtil.camelToSnakeCase(strategy.getName());
                                return StringUtil.toQuotedString(columnName);
                            }),
                    resultSetVar,
                    strategy.getName(),
                    true,
                    builder);
            rsVarNames[i] = rsVarName;
            if (i > 0) {
                constructorCode.append(", ");
            }
            constructorCode.append("$L");
        }
        constructorCode.append(")");
        final var params = new Object[strategies.size() + 2];
        params[0] = objectResultName;
        params[1] = TypeName.get(returnType);
        System.arraycopy(rsVarNames, 0, params, 2, rsVarNames.length);
        builder.addStatement(constructorCode.toString(), params);
    }

    private static void withPartialCustomization(final List<ConstructorStrategy> strategies,
                                                 final String objectResultName,
                                                 final TypeMirror returnType,
                                                 final String resultSetVar,
                                                 final MethodSpec.Builder builder,
                                                 final StringBuilder constructorCode,
                                                 final List<ColumnMapRangeInfo> columnMapRange) {
        final var rsVarNames = new String[strategies.size()];
        var rangeIndex = 0;
        var currentParamPos = 1;
        for (int i = 0; i < strategies.size(); i++) {
            final var strategy = strategies.get(i);

            final String rsVarName;
            if (rangeIndex < columnMapRange.size() && currentParamPos == columnMapRange.get(rangeIndex).start()) {
                final var varName = strategy.getName();
                rsVarName = "rs"+ varName.substring(0, 1).toUpperCase() + varName.substring(1);
                final var range = columnMapRange.get(rangeIndex);
                builder.addStatement("final var $L = $L($L,$L)", rsVarName, range.method(), resultSetVar, i + 1 );

                var columnsCovered = (range.end() - range.start()) + 1;

                builder.addStatement("paramIndex += $L", columnsCovered);
                currentParamPos += columnsCovered;
                rangeIndex++;

                i += (columnsCovered - 1);

                rsVarNames[i] = rsVarName;
                if (i > 0) {
                    constructorCode.append(", ");
                }
                constructorCode.append("$L");
                continue;
            } else {
                rsVarName = JDBCUtil.getResultSetGetter(
                        TypeName.get(strategy.getType()),
                        Optional.ofNullable(strategy.getResultSetIndex())
                                .map(String::valueOf)
                                .orElseGet(() -> {
                                    final var columnName = StringUtil.camelToSnakeCase(strategy.getName());
                                    return StringUtil.toQuotedString(columnName);
                                }),
                        resultSetVar,
                        strategy.getName(),
                        true,
                        builder);
            }


            rsVarNames[i] = rsVarName;
            if (i > 0) {
                constructorCode.append(", ");
            }
            constructorCode.append("$L");
        }
        constructorCode.append(")");
        final var params = new Object[strategies.size() + 2];
        params[0] = objectResultName;
        params[1] = TypeName.get(returnType);
        System.arraycopy(rsVarNames, 0, params, 2, rsVarNames.length);
        builder.addStatement(constructorCode.toString(), params);
    }

}
