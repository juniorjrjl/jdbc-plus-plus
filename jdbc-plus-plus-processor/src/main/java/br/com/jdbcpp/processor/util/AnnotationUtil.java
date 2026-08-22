package br.com.jdbcpp.processor.util;

import br.com.jdbcpp.api.DAO;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

public final class AnnotationUtil {

    private AnnotationUtil() {}

    public static boolean isNotJdbcppAnnotation(final AnnotationMirror annotation) {

        final var annotationType = (TypeElement) annotation.getAnnotationType().asElement();

        return !annotationType.getQualifiedName()
                .toString()
                .startsWith(DAO.class.getPackageName());
    }

}
