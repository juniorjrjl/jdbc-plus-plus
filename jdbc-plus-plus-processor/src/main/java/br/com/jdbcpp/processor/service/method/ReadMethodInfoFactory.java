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

public class ReadMethodInfoFactory {

    private final Types types;
    private final Elements elements;
    private final BuildConstructorStrategy buildConstructorStrategy;
    private final BuildSetterStrategy buildSetterStrategy;
    private final TypeUtil typeUtil;
    private final MethodValidator methodValidator;
    private final CollectionUtil collectionUtil;
    private final TypeMirror nullReadException;
    private final TypeMirror sqlException;

    public ReadMethodInfoFactory(final Types types,
                                 final Elements elements,
                                 final BuildConstructorStrategy buildConstructorStrategy,
                                 final BuildSetterStrategy buildSetterStrategy,
                                 final TypeUtil typeUtil,
                                 final MethodValidator methodValidator,
                                 final CollectionUtil collectionUtil,
                                 final TypeMirror nullReadException,
                                 final TypeMirror sqlException) {
        this.types = types;
        this.elements = elements;
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

                if (typeUtil.isList(listTypeMirror)) {
                    instanceContainer = method.getReturnType();
                } else {
                    instanceContainer = typeUtil.buildContainerTypeMirror(
                            resultBuildStrategy::collectionImplementationResult,
                            returnType
                    );
                }
            }

        }

        final var methodExceptionThrow = types.isSameType(nullReadException, packException) ?
                sqlException:
                packException;
        final MethodInfo methodInfo = needStrategyToSelectReturn(returnType) ?
                objectSelectResult(method, params, classPropertyMap, query, returnType, returnContainerType, instanceContainer, methodExceptionThrow):
                simpleSelectResult(method, params, classPropertyMap, query, returnType, returnContainerType, instanceContainer, methodExceptionThrow);
        methodValidator.validateParams(
                method,
                params,
                classPropertyMap,
                methodInfo.getStatement().params()
        );
        methodValidator.validateExceptionThrow(method, methodExceptionThrow);

        return methodInfo;
    }

    private SelectMethodInfo objectSelectResult(final ExecutableElement method,
                                                final List<ParamInfo> params,
                                                final Map<String, List<ParamInfo>> classPropertyMap,
                                                final Query query,
                                                final TypeMirror returnType,
                                                @Nullable
                                                final TypeMirror returnContainerType,
                                                @Nullable
                                                final TypeMirror instanceContainer,
                                                final TypeMirror packException) throws InvalidSelectResultMappingException {
        final var resultBuildStrategy = method.getAnnotation(ResultBuildStrategy.class);
        final var strategyType = determineStrategyType(returnType, resultBuildStrategy);
        final var typeElement = ((TypeElement) types.asElement(returnType));
        final var methodName = method.getSimpleName().toString();
        final var strategies = strategyType == CONSTRUCTOR ?
                buildConstructorStrategy.generateStrategyInfo(returnType, methodName) :
                buildSetterStrategy.generateStrategyInfo(typeElement);
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

    private SelectMethodInfo simpleSelectResult(final ExecutableElement method,
                                                final List<ParamInfo> params,
                                                final Map<String, List<ParamInfo>> classPropertyMap,
                                                final Query query,
                                                final TypeMirror returnType,
                                                @Nullable
                                                final TypeMirror returnContainerType,
                                                @Nullable
                                                final TypeMirror instanceContainer,
                                                final TypeMirror packException) {
        final var genericType = Optional.ofNullable(collectionUtil.getCollectionElementType(returnType))
                .or(() -> Optional.ofNullable(typeUtil.getOptionalType(returnType)))
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
