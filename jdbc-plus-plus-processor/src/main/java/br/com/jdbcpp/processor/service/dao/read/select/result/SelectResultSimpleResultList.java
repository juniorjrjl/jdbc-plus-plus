package br.com.jdbcpp.processor.service.dao.read.select.result;

import br.com.jdbcpp.processor.dto.result.SimpleResultStrategy;
import br.com.jdbcpp.processor.util.JDBCUtil;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;

import java.util.List;

public class SelectResultSimpleResultList {

    public void build(final List<SimpleResultStrategy> strategies,
                      final String resultSetVar,
                      final MethodSpec.Builder builder) {
        final var strategy = strategies.getFirst();
        JDBCUtil.getResultSetGetter(
                TypeName.get(strategy.getType()),
                "0",
                resultSetVar,
                "model",
                false,
                builder);
    }
}
