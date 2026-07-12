package br.com.jdbcpp.processor.service.read.select.result;

import br.com.jdbcpp.processor.dto.result.SimpleResultStrategy;
import br.com.jdbcpp.processor.util.JDBCUtil;
import br.com.jdbcpp.processor.util.StringUtil;
import com.palantir.javapoet.MethodSpec;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

public class SelectResultSimpleResultList {

    public void build(final List<SimpleResultStrategy> strategies,
                      final String objectResultName,
                      final TypeMirror returnType,
                      final String resultSetVar,
                      final MethodSpec.Builder builder) {
        final var strategy = strategies.getFirst();
        JDBCUtil.getResultSetGetter(
                strategy.getType(),
                "0",
                resultSetVar,
                "model",
                false,
                builder);
    }
}
