package br.com.jdbcpp.processor.util;

import br.com.jdbcpp.api.PropStrategy;
import br.com.jdbcpp.processor.dto.ParamKind;
import br.com.jdbcpp.processor.dto.result.SelectReturnStrategy;
import br.com.jdbcpp.processor.dto.result.SetterStrategy;
import br.com.jdbcpp.processor.exception.InvalidSelectResultMappingException;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import static br.com.jdbcpp.processor.dto.ParamKind.COLLECTION_JAVA_TYPE;
import static br.com.jdbcpp.processor.dto.ParamKind.COLLECTION_NESTED;
import static br.com.jdbcpp.processor.dto.ParamKind.JAVA_TYPE;
import static br.com.jdbcpp.processor.dto.ParamKind.NESTED_OBJECT;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

public final class BuildSetterStrategy {

    private BuildSetterStrategy() {}

    public static List<SelectReturnStrategy<?>> generateStrategyInfo(final TypeElement typeElement,
                                                                     final Types types) {

        final List<SelectReturnStrategy<?>> strategies = new ArrayList<>();
        final var useIndexBasedAccess = shouldUseIndexBasedAccess(typeElement);

        for (final var enclosedElement : typeElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.FIELD) {
                final var field = (VariableElement) enclosedElement;
                final var fieldName = field.getSimpleName().toString();
                final var fieldType = field.asType();
                final var propStrategy = requireNonNull(
                        field.getAnnotation(PropStrategy.class),
                        "@PropStrategy annotation must not be null"
                );

                if (propStrategy.ignore()) {
                    continue;
                }

                final var setterMethod = findSetterMethod(typeElement, field, propStrategy, types);
                if (setterMethod.isEmpty()) {
                    final var message = String.format(
                            "no setter found for field '%s' in class %s, create one or mapping using 'PropStrategy.setter'",
                            fieldName,
                            typeElement.getQualifiedName()
                    );
                    throw new InvalidSelectResultMappingException(message);
                }

                final var paramKind = determineParamKind(fieldType, types);
                final var genericType = CollectionUtil.getCollectionElementType(fieldType);
                final var resultSetIndex = useIndexBasedAccess ? propStrategy.resultSetIndex() : null;

                strategies.add(new SetterStrategy(
                        setterMethod.get().getSimpleName().toString(),
                        fieldName,
                        TypeName.get(fieldType),
                        paramKind,
                        List.of(),
                        isNull(genericType) ? null : TypeName.get(genericType),
                        resultSetIndex
                ));
            }
        }

        return strategies;
    }

    private static boolean shouldUseIndexBasedAccess(final TypeElement typeElement) {

        final var fields = typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .map(e -> (VariableElement) e)
                .toList();

        if (fields.isEmpty()) {
            final var message = String.format(
                    "No fields found in result class %s",
                    typeElement.getQualifiedName()
            );
            throw new InvalidSelectResultMappingException(message);
        }

        if (fields.stream().map(f -> f.getAnnotation(PropStrategy.class))
                .anyMatch(p -> isNull(p) || (!p.ignore() && p.resultSetIndex() == -1))){
            return false;
        }

        final var rsIndexes = fields.stream().map(f -> f.getAnnotation(PropStrategy.class))
                .filter(Objects::nonNull)
                .map(PropStrategy::resultSetIndex)
                .toList();

        final var min = Collections.min(rsIndexes);
        if (min != 0){
            final var message = String.format(
                    "A class %s must have a minimum result set index of 0",
                    typeElement.getQualifiedName()
            );
            throw new InvalidSelectResultMappingException(message);
        }

        final var max = Collections.min(rsIndexes);
        final var hasAllIndexes = IntStream.range(min, max).allMatch(rsIndexes::contains);
        if (!hasAllIndexes) {
            final var message = String.format(
                    "A result set not using sequential numbers in class %s",
                    typeElement.getQualifiedName()
            );
            throw new InvalidSelectResultMappingException(message);
        }

        return true;
    }

    private static Optional<ExecutableElement> findSetterMethod(final TypeElement typeElement,
                                                                final VariableElement field,
                                                                @Nullable
                                                                final PropStrategy propStrategy,
                                                                final Types types) {

        final var fieldType = field.asType();
        final var fieldName = field.getSimpleName().toString();

        if (nonNull(propStrategy) && !propStrategy.setter().isEmpty()) {
            return findMethodByName(typeElement, propStrategy.setter());
        }

        final var setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        final var byPrefix = findMethodByNameAndParameterType(typeElement, setterName, fieldType, types);
        if (byPrefix.isPresent()) {
            return byPrefix;
        }

        return findMethodByNameAndParameterType(typeElement, fieldName, fieldType, types);
    }

    private static ParamKind determineParamKind(final TypeMirror type, final Types types) {
        if (CollectionUtil.isCollectionType(type, types)) {
            final var elementType = CollectionUtil.getCollectionElementType(type);
            if (nonNull(elementType) && TypeUtil.isNestedObjectType(elementType, types)) {
                return COLLECTION_NESTED;
            }
            return COLLECTION_JAVA_TYPE;
        }
        if (TypeUtil.isNestedObjectType(type, types)) {
            return NESTED_OBJECT;
        }
        return JAVA_TYPE;
    }

    private static Optional<ExecutableElement> findMethodByName(final TypeElement typeElement, final String methodName) {

        return typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .map(e -> (ExecutableElement) e)
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .findFirst();
    }

    private static Optional<ExecutableElement> findMethodByNameAndParameterType(final TypeElement typeElement,
                                                                                final String methodName,
                                                                                final TypeMirror parameterType,
                                                                                final Types types) {

        return typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .map(e -> (ExecutableElement) e)
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .filter(m -> m.getParameters().size() == 1)
                .filter(m -> types.isSameType(m.getParameters().getFirst().asType(), parameterType))
                .findFirst();
    }

}
