package br.com.jdbcpp.processor.util;

import org.jspecify.annotations.Nullable;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Objects.isNull;

public class TypeUtil {

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

    private static final Set<String> JDBC_COMPATIBLE_TYPES = Set.of(
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
            java.time.ZonedDateTime.class.getCanonicalName(),
            byte[].class.getCanonicalName(),
            Byte[].class.getCanonicalName()
    );

    private static final Set<String> OPTIONAL_TYPES = Set.of(
            java.util.Optional.class.getCanonicalName()
    );

    private final Elements elements;
    private final Types types;
    private final CollectionUtil collectionUtil;

    public TypeUtil(final Elements elements,
                    final Types types,
                    final CollectionUtil collectionUtil) {
        this.elements = elements;
        this.types = types;
        this.collectionUtil = collectionUtil;
    }

    public boolean isSimpleType(final TypeMirror type) {
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

    public boolean isNotSimpleType(final TypeMirror type) {
        return !isSimpleType(type);
    }

    public boolean isEnum(final TypeMirror type){

        final var element = types.asElement(type);
        if (!(element instanceof TypeElement typeElement)) {
            return false;
        }

        return isEnum(typeElement);
    }

    private boolean isEnum(final TypeElement typeElement){
        return (typeElement.getKind() == ElementKind.ENUM);
    }

    public boolean isNestedObjectType(final TypeMirror type) {
        return !isSimpleType(type) && !collectionUtil.isCollectionType(type);
    }

    public boolean isOptionalType(final TypeMirror type) {
        final var element = types.asElement(type);
        if (!(element instanceof TypeElement typeElement)) {
            return false;
        }

        return OPTIONAL_TYPES.contains(typeElement.getQualifiedName().toString());
    }

    @Nullable
    public TypeMirror getOptionalType(final TypeMirror typeMirror) {
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
    public TypeMirror getOptionalElementType(final TypeMirror type) {
        if (!(type instanceof DeclaredType declaredType)) {
            return null;
        }

        final var typeArgs = declaredType.getTypeArguments();
        if (typeArgs.isEmpty()) {
            return null;
        }

        return typeArgs.getFirst();
    }

    public boolean isRecord(final TypeMirror type) {
        final var element = types.asElement(type);
        if (!(element instanceof TypeElement typeElement)) {
            return false;
        }

        return typeElement.getKind() == ElementKind.RECORD;
    }

    public String getSimpleClassName(final String qualifiedName){
        final var lastDotIndex = qualifiedName.lastIndexOf(".");
        return lastDotIndex == -1 ?
                qualifiedName :
                qualifiedName.substring(lastDotIndex + 1);
    }

    public boolean typeHasTypeParameter(final TypeElement type){
        return type.getTypeParameters().isEmpty();
    }

    public TypeMirror buildContainerTypeMirror(final Supplier<Class<? extends Collection>> containerType,
                                               final TypeMirror elementType){
        TypeMirror listTypeMirror;
        try{
            final var typeElement = elements.getTypeElement(containerType.get().getCanonicalName());
            listTypeMirror = typeElement.asType();
        } catch (final MirroredTypeException e){
            listTypeMirror = e.getTypeMirror();
        }
        final var collectionElement = (TypeElement) types.asElement(listTypeMirror);
        return types.getDeclaredType(collectionElement, elementType);
    }

    public boolean isList(TypeMirror typeMirror) {
        if (typeMirror instanceof DeclaredType declaredType) {
            final var element = declaredType.asElement();
            if (element instanceof TypeElement typeElement) {
                return typeElement.getQualifiedName().contentEquals("java.util.List");
            }
        }
        return false;
    }

    public TypeMirror getTypeMirrorFromClass(final Supplier<Class<?>> classCallback){
        TypeMirror typeMirror = null;
        try{
            final var typeElement = elements.getTypeElement(classCallback.get().getCanonicalName());
            typeMirror = typeElement.asType();
        } catch (final MirroredTypeException e){
            typeMirror = e.getTypeMirror();
        }
        return typeMirror;
    }

    public boolean isCollectionOfClass(final TypeMirror type) {
        final var elementType = collectionUtil.getCollectionElementType(type);
        if (isNull(elementType)) {
            return false;
        }
        return isNotSimpleType(elementType);
    }

}
