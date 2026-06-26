package br.com.jdbcpp.processor.service.statement;

import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfo;
import com.palantir.javapoet.MethodSpec;

import java.util.List;

public interface StatementResolver {

    String resolveParamPath(final String queryParamName);

    SimpleParamInfo getParamInfo(final String queryParamName);

    void buildCollectionSizes(final MethodSpec.Builder methodBuilder, final List<String> sql);

}
