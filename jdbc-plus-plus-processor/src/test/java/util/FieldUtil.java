package util;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public final class FieldUtil {

    private FieldUtil() {}

    public static TypeMirror getFieldType(final TypeElement fixture, final String fieldName) {
        return fixture.getEnclosedElements().stream()
                .filter(e -> e.getSimpleName().contentEquals(fieldName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Field not found: " + fieldName))
                .asType();
    }

}
