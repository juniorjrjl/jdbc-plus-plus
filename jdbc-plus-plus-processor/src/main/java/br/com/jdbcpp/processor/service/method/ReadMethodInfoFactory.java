package br.com.jdbcpp.processor.service.method;

import br.com.jdbcpp.api.Query;
import br.com.jdbcpp.api.ResultBuildStrategy;
import br.com.jdbcpp.api.ResultBuildStrategyType;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.result.SelectReturnStrategy;
import br.com.jdbcpp.processor.dto.result.SimpleResultStrategy;
import br.com.jdbcpp.processor.exception.InvalidInputParamException;
import br.com.jdbcpp.processor.exception.InvalidMethodSignatureException;
import br.com.jdbcpp.processor.exception.InvalidSelectResultMappingException;
import br.com.jdbcpp.processor.exception.MoreParamsThanStatementNeedException;
import br.com.jdbcpp.processor.service.statement.StatementInfoFactory;
import br.com.jdbcpp.processor.service.validation.MethodValidator;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.TypeUtil;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static br.com.jdbcpp.api.ResultBuildStrategyType.CONSTRUCTOR;
import static br.com.jdbcpp.api.ResultBuildStrategyType.SETTER;
import static br.com.jdbcpp.api.ResultBuildStrategyType.SIMPLE_RESULT;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class ReadMethodInfoFactory {

    private final Types types;
    private final BuildConstructorStrategy buildConstructorStrategy;
    private final BuildSetterStrategy buildSetterStrategy;
    private final TypeUtil typeUtil;
    private final MethodValidator methodValidator;
    private final CollectionUtil collectionUtil;
    private final TypeMirror nullReadException;
    private final TypeMirror sqlException;

    public ReadMethodInfoFactory(final Types types,
                                 final BuildConstructorStrategy buildConstructorStrategy,
                                 final BuildSetterStrategy buildSetterStrategy,
                                 final TypeUtil typeUtil,
                                 final MethodValidator methodValidator,
                                 final CollectionUtil collectionUtil,
                                 final TypeMirror nullReadException,
                                 final TypeMirror sqlException) {
        this.types = types;
        this.buildConstructorStrategy = buildConstructorStrategy;
        this.buildSetterStrategy = buildSetterStrategy;
        this.typeUtil = typeUtil;
        this.methodValidator = methodValidator;
        this.collectionUtil = collectionUtil;
        this.nullReadException = nullReadException;
        this.sqlException = sqlException;
    }

    public MethodInfo create(final ExecutableElement method,
                             final List<ParamInfo> params,
                             final Map<String, List<ParamInfo>> classPropertyMap,
                             final Query query,
                             final TypeMirror packException) throws InvalidMethodSignatureException,
            InvalidInputParamException,
            MoreParamsThanStatementNeedException,
            InvalidSelectResultMappingException {

        if (method.getReturnType().getKind() == TypeKind.VOID) {
            final var message = String.format(
                    "Method %s is annotated with @Query but returns void",
                    method.getSimpleName()
            );
            throw new InvalidMethodSignatureException(message, method);
        }

        final var returnType = methodReturnType(method);
        final var returnContainerType = methodReturnContainerType(method);
        final var instanceContainer = methodInstanceContainer(method, returnType);

        final var methodExceptionThrow = types.isSameType(nullReadException, packException) ?
                sqlException:
                packException;

        final var builder = MethodInfo.builder()
                .withName(method.getSimpleName().toString())
                .withReturnType(returnType)
                .withParams(params)
                .withClassPropertyMap(classPropertyMap)
                .withStatement(StatementInfoFactory.create(query.value()))
                .withPackException(methodExceptionThrow)
                .asReadType()
                .withContainerReturnTypeMirror(returnContainerType)
                .withInstanceContainer(instanceContainer);

        final MethodInfo methodInfo = needStrategyToSelectReturn(returnType) ?
                objectSelectResult(builder, method, returnType):
                simpleSelectResult(builder, returnType);

        methodValidator.validateParams(method, params, classPropertyMap, methodInfo.getStatement().params());
        methodValidator.validateExceptionThrow(method, methodExceptionThrow);

        return methodInfo;
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
        TypeMirror instanceContainer = null;
        if (method.getReturnType() instanceof DeclaredType returnTypeElement &&
                !returnTypeElement.getTypeArguments().isEmpty()){
            final var resultBuildStrategy= method.getAnnotation(ResultBuildStrategy.class);
            instanceContainer = method.getReturnType();

            if (nonNull(resultBuildStrategy)) {
                @SuppressWarnings("rawtypes")
                final Supplier<Class<? extends Collection>> containerType =
                        resultBuildStrategy::collectionImplementationResult;
                final var listTypeMirror = typeUtil.getTypeMirrorFromName(containerType);

                if (typeUtil.isNotList(listTypeMirror)) {
                    instanceContainer = typeUtil.buildContainerTypeMirror(containerType, returnType);
                }
            }
        }
        return instanceContainer;
    }

    private SelectMethodInfo objectSelectResult(final SelectMethodInfo.SelectMethodInfoBuilder selectMethodInfoBuilder,
                                                final ExecutableElement method,
                                                final TypeMirror returnType) throws InvalidSelectResultMappingException {
        final var resultBuildStrategy = method.getAnnotation(ResultBuildStrategy.class);
        final var strategyType = determineStrategyType(returnType, resultBuildStrategy);
        final var typeElement = ((TypeElement) types.asElement(returnType));
        final var methodName = method.getSimpleName().toString();
        final var strategies = strategyType == CONSTRUCTOR ?
                buildConstructorStrategy.generateStrategyInfo(returnType, methodName) :
                buildSetterStrategy.generateStrategyInfo(typeElement);
        return selectMethodInfoBuilder
                .withStrategies(strategies)
                .withStrategyType(strategyType)
                .build();
    }

    private SelectMethodInfo simpleSelectResult(final SelectMethodInfo.SelectMethodInfoBuilder selectMethodInfoBuilder,
                                                final TypeMirror returnType) {
        final var genericType = Optional.ofNullable(collectionUtil.getCollectionElementType(returnType))
                .or(() -> Optional.ofNullable(typeUtil.getOptionalType(returnType)))
                .orElse(null);
        final SelectReturnStrategy<SimpleResultStrategy> strategy = new SimpleResultStrategy(returnType, genericType);
        return selectMethodInfoBuilder
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
