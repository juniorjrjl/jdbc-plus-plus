package br.com.jdbcpp.processor.service.dao.statement.param;

import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfo;
import com.palantir.javapoet.MethodSpec;

import java.util.List;

public interface StatementParamResolver {

    String resolveParamPath(final String queryParamName);

    SimpleParamInfo getParamInfo(final String queryParamName);

    void buildCollectionSizes(final MethodSpec.Builder methodBuilder, final List<String> sql);

}
