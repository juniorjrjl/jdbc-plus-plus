package br.com.jdbcpp.processor.dto.constructor;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;
import java.util.List;

public record ConstructorParamInfo(
        String name,
        TypeMirror type,
        List<? extends AnnotationMirror> annotations
) {
}
