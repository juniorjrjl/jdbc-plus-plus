package br.com.jdbcpp.processor.util;

import org.jspecify.annotations.Nullable;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Optional;
import java.util.Set;

public final class TypeUtil {

    private TypeUtil() {}

    private static final Set<String> SIMPLE_TYPES = Set.of(
            String.class.getCanonicalName(),

            Boolean.class.getCanonicalName(),
            Byte.class.getCanonicalName(),
            Character.class.getCanonicalName(),
            Short.class.getCanonicalName(),
            Integer.class.getCanonicalName(),
            Long.class.getCanonicalName(),
            Float.class.getCanonicalName(),
            Double.class.getCanonicalName(),

            java.math.BigDecimal.class.getCanonicalName(),
            java.math.BigInteger.class.getCanonicalName(),

            java.util.Date.class.getCanonicalName(),
            java.util.UUID.class.getCanonicalName(),

            java.time.Instant.class.getCanonicalName(),
            java.time.LocalDate.class.getCanonicalName(),
            java.time.LocalDateTime.class.getCanonicalName(),
            java.time.LocalTime.class.getCanonicalName(),
            java.time.OffsetDateTime.class.getCanonicalName(),
            java.time.OffsetTime.class.getCanonicalName(),
            java.time.ZonedDateTime.class.getCanonicalName()
    );

    private static final Set<String> OPTIONAL_TYPES = Set.of(
            java.util.Optional.class.getCanonicalName()
    );

    public static boolean isSimpleType(final TypeMirror type, final Types types) {
        if (type.getKind().isPrimitive()) {
            return true;
        }

        final var element = types.asElement(type);
        if (!(element instanceof TypeElement typeElement)) {
            return false;
        }

        if (isEnum(typeElement)) {
            return true;
        }

        return SIMPLE_TYPES.contains(typeElement.getQualifiedName().toString());
    }

    public static boolean isNotSimpleType(final TypeMirror type, final Types types) {
        return !isSimpleType(type, types);
    }

    public static boolean isEnum(final TypeMirror type, final Types types){

        final var element = types.asElement(type);
        if (!(element instanceof TypeElement typeElement)) {
            return false;
        }

        return isEnum(typeElement);
    }

    private static boolean isEnum(final TypeElement typeElement){
        return (typeElement.getKind() == ElementKind.ENUM);
    }

    public static boolean isNestedObjectType(final TypeMirror type, final Types types) {
        return !isSimpleType(type, types) && !CollectionUtil.isCollectionType(type, types);
    }

    public static boolean isOptionalType(final TypeMirror type, final Types types) {
        final var element = types.asElement(type);
        if (!(element instanceof TypeElement typeElement)) {
            return false;
        }

        return OPTIONAL_TYPES.contains(typeElement.getQualifiedName().toString());
    }

    @Nullable
    public static TypeMirror getOptionalType(final TypeMirror typeMirror) {
        if (!(typeMirror instanceof DeclaredType declaredType)) {
            return null;
        }

        if (!declaredType.asElement().toString().equals(Optional.class.getCanonicalName())) {
            return null;
        }

        final var typeArguments = declaredType.getTypeArguments();

        if (typeArguments.size() != 1) {
            return null;
        }

        return typeArguments.getFirst();
    }

    @Nullable
    public static TypeMirror getOptionalElementType(final TypeMirror type, final Types types) {
        if (!(type instanceof DeclaredType declaredType)) {
            return null;
        }

        final var typeArgs = declaredType.getTypeArguments();
        if (typeArgs.isEmpty()) {
            return null;
        }

        return typeArgs.getFirst();
    }

    public static boolean isRecord(final TypeMirror type, final Types types) {
        final var element = types.asElement(type);
        if (!(element instanceof TypeElement typeElement)) {
            return false;
        }

        return typeElement.getKind() == ElementKind.RECORD;
    }

}
