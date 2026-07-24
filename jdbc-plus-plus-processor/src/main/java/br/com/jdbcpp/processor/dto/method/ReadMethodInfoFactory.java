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
import br.com.jdbcpp.processor.util.MethodValidator;
import br.com.jdbcpp.processor.util.TypeUtil;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
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
                                    final Types types,
                                    final Elements elements,
                                    final TypeMirror packException) {

        if (method.getReturnType().getKind() == TypeKind.VOID) {
            final var message = String.format(
                    "Method %s is annotated with @Query but returns void",
                    method.getSimpleName()
            );
            throw new InvalidMethodSignatureException(message);
        }

        TypeMirror returnType = method.getReturnType();
        TypeMirror returnContainerType = null;
        TypeMirror instanceContainer = null;
        if (method.getReturnType() instanceof DeclaredType returnTypeElement &&
                !returnTypeElement.getTypeArguments().isEmpty()){
            final var resultBuildStrategy= method.getAnnotation(ResultBuildStrategy.class);
            returnType = returnTypeElement.getTypeArguments().getFirst();
            returnContainerType = method.getReturnType();
            instanceContainer = method.getReturnType();
            if (nonNull(resultBuildStrategy)){
                TypeMirror listTypeMirror;
                try{
                    final var canonicalName = resultBuildStrategy.collectionImplementationResult().getCanonicalName();
                    final var typeElement = elements.getTypeElement(canonicalName);
                    listTypeMirror = typeElement.asType();
                } catch (final MirroredTypeException e){
                    listTypeMirror = e.getTypeMirror();
                }

                if (TypeUtil.isList(listTypeMirror)) {
                    instanceContainer = method.getReturnType();
                } else {
                    instanceContainer = TypeUtil.buildContainerTypeMirror(
                            resultBuildStrategy::collectionImplementationResult,
                            returnType,
                            elements,
                            types
                    );
                }
            }

        }

        final MethodInfo methodInfo = needStrategyToSelectReturn(returnType, types) ?
                objectSelectResult(method, params, classPropertyMap, query, types, returnType, returnContainerType, instanceContainer, packException):
                simpleSelectResult(method, params, classPropertyMap, query, returnType, returnContainerType, instanceContainer, packException);
        MethodValidator.validateParams(
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
                                                       final TypeMirror returnType,
                                                       @Nullable
                                                       final TypeMirror returnContainerType,
                                                       @Nullable
                                                       final TypeMirror instanceContainer,
                                                       final TypeMirror packException) {
        final var resultBuildStrategy = method.getAnnotation(ResultBuildStrategy.class);
        final var strategyType = determineStrategyType(types, returnType, resultBuildStrategy);
        final var typeElement = ((TypeElement) types.asElement(returnType));
        final var methodName = method.getSimpleName().toString();
        final var strategies = strategyType == CONSTRUCTOR ?
                BuildConstructorStrategy.generateStrategyInfo(returnType, types, methodName) :
                BuildSetterStrategy.generateStrategyInfo(typeElement, types);
        return new SelectMethodInfo(
                methodName,
                returnType,
                params,
                classPropertyMap,
                StatementInfoFactory.create(query.value()),
                packException,
                strategies,
                strategyType,
                returnContainerType,
                instanceContainer
        );
    }

    private static SelectMethodInfo simpleSelectResult(final ExecutableElement method,
                                                       final List<ParamInfo> params,
                                                       final Map<String, List<ParamInfo>> classPropertyMap,
                                                       final Query query,
                                                       final TypeMirror returnType,
                                                       @Nullable
                                                       final TypeMirror returnContainerType,
                                                       @Nullable
                                                       final TypeMirror instanceContainer,
                                                       final TypeMirror packException) {
        final var genericType = Optional.ofNullable(CollectionUtil.getCollectionElementType(returnType))
                .or(() -> Optional.ofNullable(TypeUtil.getOptionalType(returnType)))
                .orElse(null);
        final SelectReturnStrategy<SimpleResultStrategy> strategy = new SimpleResultStrategy(returnType, genericType);
        return new SelectMethodInfo(
                method.getSimpleName().toString(),
                returnType,
                params,
                classPropertyMap,
                StatementInfoFactory.create(query.value()),
                packException,
                strategy,
                returnContainerType,
                instanceContainer
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
