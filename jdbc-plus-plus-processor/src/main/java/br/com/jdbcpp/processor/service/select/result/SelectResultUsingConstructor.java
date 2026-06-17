package br.com.jdbcpp.processor.service.select.result;

import br.com.jdbcpp.processor.dto.result.ConstructorStrategy;
import br.com.jdbcpp.processor.dto.result.SelectReturnStrategy;
import br.com.jdbcpp.processor.util.JDBCUtil;
import br.com.jdbcpp.processor.util.StringUtil;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

public class SelectResultUsingConstructor {

    public void build(final List<ConstructorStrategy> strategies,
                      final String objectResultName,
                      final TypeMirror returnType,
                      final String resultSetVar,
                      final MethodSpec.Builder builder) {
        final var constructorCode = new StringBuilder("final var $L = new $T(");
        for (int i = 0; i < strategies.size(); i++) {
            final var strategy = strategies.get(i);
            final var resultSetGetter = JDBCUtil.getResultSetGetter(
                    strategy.getType(),
                    Optional.ofNullable(strategy.getResultSetIndex())
                            .map(String::valueOf)
                            .orElseGet(() ->{
                                final var columnName = StringUtil.camelToSnakeCase(strategy.getName());
                                return StringUtil.toQuotedString(columnName);
                            }),
                    resultSetVar);
            builder.addStatement("final var $L = $L", strategy.getName(), resultSetGetter);
            if (i > 0) {
                constructorCode.append(", ");
            }
            constructorCode.append("$L");
        }
        constructorCode.append(")");
        final var params = new Object[strategies.size() + 2];
        params[0] = objectResultName;
        params[1] = TypeName.get(returnType);
        final var names = strategies.stream().map(SelectReturnStrategy::getName).toArray();
        System.arraycopy(names, 0, params, 2, names.length);
        builder.addStatement(constructorCode.toString(), params);
    }

}
