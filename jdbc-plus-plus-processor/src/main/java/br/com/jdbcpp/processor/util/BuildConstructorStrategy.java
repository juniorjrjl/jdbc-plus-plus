package br.com.jdbcpp.processor.util;

import br.com.jdbcpp.processor.dto.ParamKind;
import br.com.jdbcpp.processor.dto.result.ConstructorStrategy;
import br.com.jdbcpp.processor.dto.result.SelectReturnStrategy;
import br.com.jdbcpp.processor.exception.InvalidSelectResultMapping;
import com.palantir.javapoet.TypeName;

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

    public static List<SelectReturnStrategy<?>> generateStrategyInfo(final TypeElement typeElement,
                                                                     final Types types,
                                                                     final String methodName) throws InvalidSelectResultMapping {

        final List<SelectReturnStrategy<?>> strategies = new ArrayList<>();
        final var constructors = typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
                .map(e -> (ExecutableElement) e)
                .toList();

        if (constructors.isEmpty()) {
            final var message = String.format(
                    "For use constructor strategy, a class %s must have a constructor",
                    typeElement.getQualifiedName()
            );
            throw new InvalidSelectResultMapping(message);
        }

        final var canonicalConstructor = constructors.stream()
                .filter(c -> c.getParameters().size() == typeElement.getRecordComponents().size())
                .findFirst()
                .orElseThrow(() -> {
                    final var message = String.format(
                            "A class %s have non constructor compatible with method %s",
                            typeElement.getQualifiedName(),
                            methodName
                    );
                    return new InvalidSelectResultMapping(message);
                });

        final var parameters = canonicalConstructor.getParameters();
        if (parameters.isEmpty()){
            final var message = String.format(
                    "For use constructor strategy, a class %s must have constructor with parameters",
                    typeElement.getQualifiedName()
            );
            throw new InvalidSelectResultMapping(message);
        }

        for (int i = 0; i < parameters.size(); i++) {
            final var param = parameters.get(i);
            final var paramType = param.asType();
            final var paramName = param.getSimpleName().toString();
            final var type = TypeName.get(paramType);

            final var paramKind = determineParamKind(paramType, types);
            final var genericType = Optional.of(paramType)
                    .map(CollectionUtil::getCollectionElementType)
                    .map(TypeName::get)
                    .orElse(null);

            strategies.add(new ConstructorStrategy(paramName, type, paramKind, List.of(), genericType, i));
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
