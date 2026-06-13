package br.com.jdbcpp.processor.util;

import org.jspecify.annotations.Nullable;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import static java.util.Objects.isNull;

public final class ArrayUtil {

    private ArrayUtil() {}

    public static boolean isArray(final TypeMirror type){
        return type.getKind() == TypeKind.ARRAY;
    }

    public static boolean isNotArray(final TypeMirror type){
        return !isArray(type);
    }

    @Nullable
    public static TypeMirror getArrayElementType(final TypeMirror type) {
        if (isNotArray(type)) {
            return null;
        }

        return ((ArrayType) type).getComponentType();
    }

    public static boolean isArrayOfClass(final TypeMirror type, final Types types) {
        final var element = getArrayElementType(type);
        if (isNull(element)) {
            return false;
        }
        return TypeUtil.isNotSimpleType(element, types);
    }

}
