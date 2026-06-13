package br.com.jdbcpp.processor.util;

import org.jspecify.annotations.Nullable;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;

public final class CollectionUtil {

    private static final Set<String> COLLECTION_TYPES = Set.of(
            java.util.Collection.class.getCanonicalName(),
            java.util.List.class.getCanonicalName(),
            java.util.Set.class.getCanonicalName(),
            java.util.ArrayList.class.getCanonicalName(),
            java.util.HashSet.class.getCanonicalName(),
            java.util.LinkedList.class.getCanonicalName()
    );

    private CollectionUtil() {}

    public static boolean isCollectionType(final TypeMirror type, final Types types) {
        final var element = types.asElement(type);
        if (!(element instanceof TypeElement typeElement)) {
            return false;
        }

        final var qualifiedName = typeElement.getQualifiedName().toString();
        if (COLLECTION_TYPES.contains(qualifiedName)) {
            return true;
        }

        for (final var interfaceType : typeElement.getInterfaces()) {
            if (isCollectionType(interfaceType, types)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isCollectionInterface(final TypeMirror type, final Types types) {
        final var element = types.asElement(type);
        if (!(element instanceof TypeElement typeElement)) {
            return false;
        }

        final var qualifiedName = typeElement.getQualifiedName().toString();

        return qualifiedName.equals(List.class.getCanonicalName()) ||
                qualifiedName.equals(Collection.class.getCanonicalName()) ||
                qualifiedName.equals(Set.class.getCanonicalName());
    }

    public static String getCollectionImplementation(final TypeMirror type, final Types types) {
        final var element = types.asElement(type);
        if (!(element instanceof TypeElement typeElement)) {
            return ArrayList.class.getCanonicalName();
        }

        final var qualifiedName = typeElement.getQualifiedName().toString();

        if (qualifiedName.equals(List.class.getCanonicalName()) ||
                qualifiedName.equals(Collection.class.getCanonicalName())) {
            return ArrayList.class.getCanonicalName();
        }

        if (qualifiedName.equals(java.util.Set.class.getCanonicalName())) {
            return HashSet.class.getCanonicalName();
        }

        return qualifiedName;
    }

    @Nullable
    public static TypeMirror getCollectionElementType(final TypeMirror type) {
        if (!(type instanceof DeclaredType declaredType)) {
            return null;
        }

        final var typeArgs = declaredType.getTypeArguments();
        if (typeArgs.isEmpty()) {
            return null;
        }

        return typeArgs.getFirst();
    }

    public static boolean isCollectionOfClass(final TypeMirror type, final Types types) {
        final var elementType = getCollectionElementType(type);
        if (isNull(elementType)) {
            return false;
        }
        return TypeUtil.isNotSimpleType(elementType, types);
    }

}
