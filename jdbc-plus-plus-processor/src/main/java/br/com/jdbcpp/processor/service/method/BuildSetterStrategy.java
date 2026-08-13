package br.com.jdbcpp.processor.service.method;

import br.com.jdbcpp.api.PropStrategy;
import br.com.jdbcpp.processor.dto.ParamKind;
import br.com.jdbcpp.processor.dto.result.SelectReturnStrategy;
import br.com.jdbcpp.processor.dto.result.SetterStrategy;
import br.com.jdbcpp.processor.exception.InvalidSelectResultMappingException;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.TypeUtil;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
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

    private final Types types;
    private final TypeUtil typeUtil;
    private final CollectionUtil collectionUtil;

    public BuildSetterStrategy(final Types types,
                               final TypeUtil typeUtil,
                               final CollectionUtil collectionUtil) {
        this.types = types;
        this.typeUtil = typeUtil;
        this.collectionUtil = collectionUtil;
    }

    public List<SelectReturnStrategy<?>> generateStrategyInfo(final TypeElement typeElement) throws InvalidSelectResultMappingException {

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

                final var setterMethod = findSetterMethod(typeElement, field, propStrategy);
                if (setterMethod.isEmpty()) {
                    final var message = String.format(
                            "no setter found for field '%s' in class %s, create one or mapping using 'PropStrategy.setter'",
                            fieldName,
                            typeElement.getQualifiedName()
                    );
                    throw new InvalidSelectResultMappingException(message);
                }

                final var paramKind = determineParamKind(fieldType);
                final var genericType = collectionUtil.getCollectionElementType(fieldType);
                final var resultSetIndex = useIndexBasedAccess ? propStrategy.resultSetIndex() : null;

                strategies.add(new SetterStrategy(
                        setterMethod.get().getSimpleName().toString(),
                        fieldName,
                        fieldType,
                        paramKind,
                        List.of(),
                        genericType,
                        resultSetIndex
                ));
            }
        }

        return strategies;
    }

    private static boolean shouldUseIndexBasedAccess(final TypeElement typeElement) throws InvalidSelectResultMappingException {

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

        final var max = Collections.max(rsIndexes);
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

    private Optional<ExecutableElement> findSetterMethod(final TypeElement typeElement,
                                                         final VariableElement field,
                                                         final PropStrategy propStrategy) {

        final var fieldType = field.asType();
        final var fieldName = field.getSimpleName().toString();

        if (!propStrategy.value().isBlank()) {
            return findMethodByName(typeElement, propStrategy.value());
        }

        final var setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        final var byPrefix = findMethodByNameAndParameterType(typeElement, setterName, fieldType);
        if (byPrefix.isPresent()) {
            return byPrefix;
        }

        return findMethodByNameAndParameterType(typeElement, fieldName, fieldType);
    }

    private ParamKind determineParamKind(final TypeMirror type) {
        if (collectionUtil.isCollectionType(type)) {
            final var elementType = collectionUtil.getCollectionElementType(type);
            if (nonNull(elementType) && typeUtil.isNestedObjectType(elementType)) {
                return COLLECTION_NESTED;
            }
            return COLLECTION_JAVA_TYPE;
        }
        if (typeUtil.isNestedObjectType(type)) {
            return NESTED_OBJECT;
        }
        return JAVA_TYPE;
    }

    private static Optional<ExecutableElement> findMethodByName(final TypeElement typeElement, final String methodName) {

        return typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .filter(e -> e.getModifiers().contains(Modifier.PUBLIC))
                .map(e -> (ExecutableElement) e)
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .findFirst();
    }

    private Optional<ExecutableElement> findMethodByNameAndParameterType(final TypeElement typeElement,
                                                                         final String methodName,
                                                                         final TypeMirror parameterType) {

        return typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .filter(e -> e.getModifiers().contains(Modifier.PUBLIC))
                .map(e -> (ExecutableElement) e)
                .filter(m -> m.getSimpleName().toString().equals(methodName))
                .filter(m -> m.getParameters().size() == 1)
                .filter(m -> types.isSameType(m.getParameters().getFirst().asType(), parameterType))
                .findFirst();
    }

}
