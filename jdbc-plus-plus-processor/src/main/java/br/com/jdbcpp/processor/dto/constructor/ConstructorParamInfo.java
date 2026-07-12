package br.com.jdbcpp.processor.dto.constructor;

import javax.lang.model.type.TypeMirror;

public record ConstructorParamInfo(
        String name,
        TypeMirror type
) {
}
