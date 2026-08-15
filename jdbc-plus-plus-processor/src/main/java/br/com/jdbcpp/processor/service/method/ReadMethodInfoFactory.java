package br.com.jdbcpp.processor.service.method;

import br.com.jdbcpp.api.Query;
import br.com.jdbcpp.api.ResultBuildStrategy;
import br.com.jdbcpp.api.ResultBuildStrategyType;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.method.SelectCollectionMethodInfo;
import br.com.jdbcpp.processor.dto.method.SelectMethodInfoBuilder;
import br.com.jdbcpp.processor.dto.method.SelectOptionalMethodInfo;
import br.com.jdbcpp.processor.dto.result.SelectReturnStrategy;
import br.com.jdbcpp.processor.dto.result.SimpleResultStrategy;
import br.com.jdbcpp.processor.exception.InvalidSelectResultMappingException;
import br.com.jdbcpp.processor.service.statement.StatementInfoFactory;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.TypeUtil;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import static br.com.jdbcpp.api.ResultBuildStrategyType.CONSTRUCTOR;
import static br.com.jdbcpp.api.ResultBuildStrategyType.SETTER;
import static br.com.jdbcpp.api.ResultBuildStrategyType.SIMPLE_RESULT;
import static br.com.jdbcpp.processor.dto.method.QueryType.COLLECTION;
import static br.com.jdbcpp.processor.dto.method.QueryType.NULLABLE;
import static br.com.jdbcpp.processor.dto.method.QueryType.OPTIONAL;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

public class ReadMethodInfoFactory {

    private final Types types;
    private final BuildConstructorStrategy buildConstructorStrategy;
    private final BuildSetterStrategy buildSetterStrategy;
    private final TypeUtil typeUtil;
    private final CollectionUtil collectionUtil;
    private final TypeMirror nullReadException;
    private final TypeMirror sqlException;
    private final TypeMirror collection;

    public ReadMethodInfoFactory(final Types types,
                                 final BuildConstructorStrategy buildConstructorStrategy,
                                 final BuildSetterStrategy buildSetterStrategy,
                                 final TypeUtil typeUtil,
                                 final CollectionUtil collectionUtil,
                                 final TypeMirror nullReadException,
                                 final TypeMirror sqlException,
                                 final TypeMirror collection) {
        this.types = types;
        this.buildConstructorStrategy = buildConstructorStrategy;
        this.buildSetterStrategy = buildSetterStrategy;
        this.typeUtil = typeUtil;
        this.collectionUtil = collectionUtil;
        this.nullReadException = nullReadException;
        this.sqlException = sqlException;
        this.collection = collection;
    }

    public MethodInfo create(final MethodInfo.MethodInfoBuilder builder,
                             final ExecutableElement method,
                             final Query query,
                             final TypeMirror packException
    ) throws InvalidSelectResultMappingException {

        final var returnType = methodReturnType(method);
        final var returnContainerType = methodReturnContainerType(method);
        final var instanceContainer = methodInstanceContainer(method, returnType);

        final var methodExceptionThrow = types.isSameType(nullReadException, packException) ?
                sqlException:
                packException;

        final var methodInfoBuilder = builder.withReturnType(returnType)
                .withStatement(StatementInfoFactory.create(query.value()))
                .withPackException(methodExceptionThrow);

        final SelectMethodInfoBuilder<?> selectMethodInfoBuilder1;
        if (nonNull(instanceContainer)){
            selectMethodInfoBuilder1 = methodInfoBuilder.<SelectCollectionMethodInfo.SelectCollectionMethodInfoBuilder>asReadType(COLLECTION)
                    .withInstanceContainer(instanceContainer)
                    .withContainerReturnTypeMirror(requireNonNull(returnContainerType, "containerReturnTypeMirror is required"));
        } else if (nonNull(returnContainerType)){
            selectMethodInfoBuilder1 = methodInfoBuilder.<SelectOptionalMethodInfo.SelectOptionalMethodInfoBuilder>asReadType(OPTIONAL)
                    .withContainerReturnTypeMirror(returnContainerType);
        } else {
            selectMethodInfoBuilder1 = methodInfoBuilder.asReadType(NULLABLE);
        }

        return needStrategyToSelectReturn(returnType) ?
                objectSelectResult(selectMethodInfoBuilder1, method, returnType):
                simpleSelectResult(selectMethodInfoBuilder1, returnType);
    }

    private TypeMirror methodReturnType(final ExecutableElement method){
        if (method.getReturnType() instanceof DeclaredType returnTypeElement &&
                !returnTypeElement.getTypeArguments().isEmpty()){
            return returnTypeElement.getTypeArguments().getFirst();
        }

        return method.getReturnType();
    }

    @Nullable
    private TypeMirror methodReturnContainerType(final ExecutableElement method){
        if (method.getReturnType() instanceof DeclaredType returnTypeElement &&
                !returnTypeElement.getTypeArguments().isEmpty()){
            return method.getReturnType();
        }

        return null;
    }

    @Nullable
    private TypeMirror methodInstanceContainer(final ExecutableElement method, final TypeMirror returnType){
        if (collectionUtil.isNotCollectionType(method.getReturnType())) {
            return null;
        }

        TypeMirror instanceContainer = null;
        if (method.getReturnType() instanceof DeclaredType returnTypeElement &&
                !returnTypeElement.getTypeArguments().isEmpty()){
            final var resultBuildStrategy= method.getAnnotation(ResultBuildStrategy.class);
            instanceContainer = method.getReturnType();

            if (nonNull(resultBuildStrategy)) {
                @SuppressWarnings("rawtypes")
                final Supplier<Class<? extends Collection>> containerType =
                        resultBuildStrategy::collectionImplementationResult;
                final var builtType = typeUtil.buildContainerTypeMirror(containerType, returnType);
                instanceContainer = types.isSameType(types.erasure(builtType), types.erasure(collection)) ?
                        method.getReturnType() :
                        builtType;
            }
        }
        return instanceContainer;
    }

    private MethodInfo objectSelectResult(final SelectMethodInfoBuilder<?> selectNullableMethodInfoBuilder,
                                                          final ExecutableElement method,
                                                          final TypeMirror returnType) throws InvalidSelectResultMappingException {
        final var resultBuildStrategy = method.getAnnotation(ResultBuildStrategy.class);
        final var strategyType = determineStrategyType(returnType, resultBuildStrategy);
        final var typeElement = ((TypeElement) types.asElement(returnType));
        final var methodName = method.getSimpleName().toString();
        final var strategies = strategyType == CONSTRUCTOR ?
                buildConstructorStrategy.generateStrategyInfo(returnType, methodName) :
                buildSetterStrategy.generateStrategyInfo(typeElement);
        return selectNullableMethodInfoBuilder
                .withStrategies(strategies)
                .withStrategyType(strategyType)
                .build();
    }

    private MethodInfo simpleSelectResult(final SelectMethodInfoBuilder<?> selectNullableMethodInfoBuilder,
                                                          final TypeMirror returnType) {
        final var genericType = Optional.ofNullable(collectionUtil.getCollectionElementType(returnType))
                .or(() -> Optional.ofNullable(typeUtil.getOptionalType(returnType)))
                .orElse(null);
        final SelectReturnStrategy<SimpleResultStrategy> strategy = new SimpleResultStrategy(returnType, genericType);
        return selectNullableMethodInfoBuilder
                .withStrategy(strategy)
                .withStrategyType(SIMPLE_RESULT)
                .build();
    }

    private boolean needStrategyToSelectReturn(final TypeMirror returnType) {
        if (typeUtil.isSimpleType(returnType)) {
            return false;
        }

        if (collectionUtil.isCollectionType(returnType)) {
            final var elementType = collectionUtil.getCollectionElementType(returnType);
            if (isNull(elementType)) {
                return false;
            }
            return typeUtil.isNotSimpleType(elementType);
        }

        return true;
    }

    private ResultBuildStrategyType determineStrategyType(final TypeMirror returnType,
                                                          @Nullable
                                                          final ResultBuildStrategy resultBuildStrategy) {
        if (typeUtil.isRecord(returnType)) {
            return CONSTRUCTOR;
        }

        if (nonNull(resultBuildStrategy)) {
            return resultBuildStrategy.value();
        }

        return SETTER;
    }

}
