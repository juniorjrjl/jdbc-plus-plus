package br.com.jdbcpp.processor.util;

import org.jspecify.annotations.Nullable;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static java.util.Objects.isNull;

public class ArrayUtil {

    private final TypeUtil typeUtil;

    public ArrayUtil(final TypeUtil typeUtil) {
        this.typeUtil = typeUtil;
    }

    public boolean isArray(final TypeMirror type){
        return type.getKind() == TypeKind.ARRAY;
    }

    public boolean isNotArray(final TypeMirror type){
        return !isArray(type);
    }

    @Nullable
    public TypeMirror getArrayElementType(final TypeMirror type) {
        if (isNotArray(type)) {
            return null;
        }

        return ((ArrayType) type).getComponentType();
    }

    public boolean isArrayOfClass(final TypeMirror type) {
        final var element = getArrayElementType(type);
        if (isNull(element)) {
            return false;
        }
        return typeUtil.isNotSimpleType(element);
    }

}
