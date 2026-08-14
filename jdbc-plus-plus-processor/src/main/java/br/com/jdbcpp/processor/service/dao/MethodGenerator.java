package br.com.jdbcpp.processor.service.dao;

import br.com.jdbcpp.processor.dto.method.MethodInfo;
import com.palantir.javapoet.MethodSpec;

public interface MethodGenerator<T extends MethodInfo> {

    boolean useInstance(final MethodInfo methodInfo);

    MethodSpec.Builder build(final T methodInfo, final String connectionCall);

}
