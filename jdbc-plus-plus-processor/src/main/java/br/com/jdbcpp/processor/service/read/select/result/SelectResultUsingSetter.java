package br.com.jdbcpp.processor.service.read.select.result;

import br.com.jdbcpp.processor.dto.result.SetterStrategy;
import br.com.jdbcpp.processor.util.JDBCUtil;
import br.com.jdbcpp.processor.util.StringUtil;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

public class SelectResultUsingSetter {

    public void build(final List<SetterStrategy> strategies,
                      final String objectResultName,
                      final TypeMirror returnType,
                      final String resultSetVar,
                      final MethodSpec.Builder builder) {
        builder.addStatement("final var $L = new $T()", objectResultName, TypeName.get(returnType));
        for (final var strategy : strategies) {
            JDBCUtil.getResultSetGetter(
                    strategy.getType(),
                    Optional.ofNullable(strategy.getResultSetIndex())
                            .map(String::valueOf)
                            .orElseGet(() ->{
                                final var columnName = StringUtil.camelToSnakeCase(strategy.getName());
                                return StringUtil.toQuotedString(columnName);
                            }),
                    resultSetVar,
                    strategy.getName(),
                    builder);
        }
    }


}
