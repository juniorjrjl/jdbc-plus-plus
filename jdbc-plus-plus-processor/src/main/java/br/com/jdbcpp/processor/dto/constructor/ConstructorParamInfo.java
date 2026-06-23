package br.com.jdbcpp.processor.dto.constructor;

import com.palantir.javapoet.TypeName;

public record ConstructorParamInfo(
        String name,
        TypeName type
) {
}
