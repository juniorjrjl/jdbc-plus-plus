package br.com.jdbcpp.processor.dto.method;

import br.com.jdbcpp.api.Query;
import br.com.jdbcpp.api.ResultBuildStrategy;
import br.com.jdbcpp.api.ResultBuildStrategyType;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.result.SelectReturnStrategy;
import br.com.jdbcpp.processor.dto.result.SimpleResultStrategy;
import br.com.jdbcpp.processor.dto.statement.StatementInfoFactory;
import br.com.jdbcpp.processor.exception.InvalidMethodSignatureException;
import br.com.jdbcpp.processor.util.BuildConstructorStrategy;
import br.com.jdbcpp.processor.util.BuildSetterStrategy;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.MethodValidatorUtil;
import br.com.jdbcpp.processor.util.TypeUtil;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static br.com.jdbcpp.api.ResultBuildStrategyType.CONSTRUCTOR;
import static br.com.jdbcpp.api.ResultBuildStrategyType.SETTER;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public final class ReadMethodInfoFactory {

    private ReadMethodInfoFactory() {
    }

    public static MethodInfo create(final ExecutableElement method,
                                    final List<ParamInfo> params,
                                    final Map<String, List<ParamInfo>> classPropertyMap,
                                    final Query query,
                                    final Types types) {
        final var returnType = method.getReturnType();
        if (returnType.getKind() == TypeKind.VOID) {
            final var message = String.format(
                    "Method %s is annotated with @Query but returns void",
                    method.getSimpleName()
            );
            throw new InvalidMethodSignatureException(message);
        }

        final MethodInfo methodInfo = needStrategyToSelectReturn(returnType, types) ?
                objectSelectResult(method, params, classPropertyMap, query, types, returnType):
                simpleSelectResult(method, params, classPropertyMap, query, returnType);
        MethodValidatorUtil.validateParams(
                methodInfo.getName(),
                params,
                classPropertyMap,
                methodInfo.getStatement().params()
        );

        return methodInfo;
    }

    private static SelectMethodInfo objectSelectResult(final ExecutableElement method,
                                                       final List<ParamInfo> params,
                                                       final Map<String, List<ParamInfo>> classPropertyMap,
                                                       final Query query,
                                                       final Types types,
                                                       final TypeMirror returnType) {
        final var resultBuildStrategy = method.getAnnotation(ResultBuildStrategy.class);
        final var strategyType = determineStrategyType(types, returnType, resultBuildStrategy);
        final var typeElement = ((TypeElement) types.asElement(returnType));
        final var methodName = method.getSimpleName().toString();
        final var strategies = strategyType == CONSTRUCTOR ?
                BuildConstructorStrategy.generateStrategyInfo(typeElement, types, methodName) :
                BuildSetterStrategy.generateStrategyInfo(typeElement, types);
        return new SelectMethodInfo(
                methodName,
                returnType,
                params,
                classPropertyMap,
                StatementInfoFactory.create(query.value()),
                strategies,
                strategyType
        );
    }

    private static SelectMethodInfo simpleSelectResult(final ExecutableElement method,
                                                       final List<ParamInfo> params,
                                                       final Map<String, List<ParamInfo>> classPropertyMap,
                                                       final Query query,
                                                       final TypeMirror returnType) {
        final var type = TypeName.get(returnType);
        final var genericType = Optional.ofNullable(CollectionUtil.getCollectionElementType(returnType))
                .or(() -> Optional.ofNullable(TypeUtil.getOptionalType(returnType)))
                .map(TypeName::get)
                .orElse(null);
        final SelectReturnStrategy<SimpleResultStrategy> strategy = new SimpleResultStrategy(type, genericType);
        return new SelectMethodInfo(
                method.getSimpleName().toString(),
                returnType,
                params,
                classPropertyMap,
                StatementInfoFactory.create(query.value()),
                strategy
        );
    }

    private static boolean needStrategyToSelectReturn(final TypeMirror returnType, final Types types) {
        if (TypeUtil.isSimpleType(returnType, types)) {
            return false;
        }

        if (CollectionUtil.isCollectionType(returnType, types)) {
            final var elementType = CollectionUtil.getCollectionElementType(returnType);
            if (isNull(elementType)) {
                return false;
            }
            return TypeUtil.isNotSimpleType(elementType, types);
        }

        return true;
    }

    private static ResultBuildStrategyType determineStrategyType(final Types types,
                                                                 final TypeMirror returnType,
                                                                 @Nullable
                                                                 final ResultBuildStrategy resultBuildStrategy) {
        if (TypeUtil.isRecord(returnType, types)) {
            return CONSTRUCTOR;
        }

        if (nonNull(resultBuildStrategy)) {
            return resultBuildStrategy.value();
        }

        return SETTER;
    }

}
