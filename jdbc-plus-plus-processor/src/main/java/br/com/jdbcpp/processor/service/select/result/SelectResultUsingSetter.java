package br.com.jdbcpp.processor.service.select.result;

import br.com.jdbcpp.processor.dto.result.SetterStrategy;
import br.com.jdbcpp.processor.util.StringUtil;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

public class SelectResultUsingSetter extends SelectResultSet<SetterStrategy>{

    public void build(final List<SetterStrategy> strategies,
                      final String objectResultName,
                      final TypeMirror returnType,
                      final String resultSetVar,
                      final MethodSpec.Builder builder) {
        builder.addStatement("final var $L = new $T()", objectResultName, TypeName.get(returnType));
        for (final var strategy : strategies) {
            final var resultSetGetter = getResultSetGetter(
                    strategy.getType(),
                    Optional.ofNullable(strategy.getResultSetIndex())
                            .map(String::valueOf)
                            .orElseGet(() ->{
                                final var columnName = StringUtil.camelToSnakeCase(strategy.getName());
                                return toQuotedString(columnName);
                            }),
                    resultSetVar);
            builder.addStatement("$L.$L($L)", objectResultName, strategy.getMethodName(), resultSetGetter);
        }
    }


}
