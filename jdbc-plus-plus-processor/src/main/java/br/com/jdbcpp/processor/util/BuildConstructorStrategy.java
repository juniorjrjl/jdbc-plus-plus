package br.com.jdbcpp.processor.util;

import br.com.jdbcpp.processor.dto.ParamKind;
import br.com.jdbcpp.processor.dto.result.ConstructorStrategy;
import br.com.jdbcpp.processor.dto.result.SelectReturnStrategy;
import br.com.jdbcpp.processor.exception.InvalidSelectResultMappingException;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static br.com.jdbcpp.processor.dto.ParamKind.COLLECTION_JAVA_TYPE;
import static br.com.jdbcpp.processor.dto.ParamKind.COLLECTION_NESTED;
import static br.com.jdbcpp.processor.dto.ParamKind.JAVA_TYPE;
import static br.com.jdbcpp.processor.dto.ParamKind.NESTED_OBJECT;
import static java.util.Objects.nonNull;

public final class BuildConstructorStrategy {

    private  BuildConstructorStrategy() {}

    public static List<SelectReturnStrategy<?>> generateStrategyInfo(final TypeMirror typeMirror,
                                                                     final Types types,
                                                                     final String methodName) throws InvalidSelectResultMappingException {

        final var typeElement = ((TypeElement) types.asElement(typeMirror));
        final List<SelectReturnStrategy<?>> strategies = new ArrayList<>();
        final var constructors = typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
                .map(e -> (ExecutableElement) e)
                .filter(e -> !e.getParameters().isEmpty())
                .toList();

        if (constructors.isEmpty()) {
            final var message = String.format(
                    "For use constructor strategy with method %s, a class %s must have a constructor",
                    methodName,
                    typeElement.getQualifiedName()
            );
            throw new InvalidSelectResultMappingException(message);
        }

        if (constructors.size() > 1) {
            final var message = String.format(
                    "For use constructor strategy with method %s, a class %s must have only one constructor",
                    methodName,
                    typeElement.getQualifiedName()
            );
            throw new InvalidSelectResultMappingException(message);
        }

        final var parameters = constructors.getFirst().getParameters();
        if (parameters.isEmpty()){
            final var message = String.format(
                    "For use constructor strategy, a class %s must have constructor with parameters",
                    typeElement.getQualifiedName()
            );
            throw new InvalidSelectResultMappingException(message);
        }

        for (int i = 0; i < parameters.size(); i++) {
            final var param = parameters.get(i);
            final var paramType = param.asType();
            final var paramName = param.getSimpleName().toString();

            final var paramKind = determineParamKind(paramType, types);
            final var genericType = Optional.of(paramType)
                    .map(CollectionUtil::getCollectionElementType)
                    .orElse(null);

            strategies.add(new ConstructorStrategy(paramName, paramType, paramKind, List.of(), genericType, i));
        }
        return strategies;
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

}
