package br.com.jdbcpp.processor.service.parameter;

import br.com.jdbcpp.api.InputParam;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfo;
import br.com.jdbcpp.processor.exception.InvalidInputParamException;
import br.com.jdbcpp.processor.util.ArrayUtil;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.LambdaUtil;
import br.com.jdbcpp.processor.util.StringUtil;
import br.com.jdbcpp.processor.util.TypeUtil;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;

public final class SimpleParamInfoFactory {

    private final Types types;
    private final Elements elements;
    private final ArrayUtil arrayUtil;
    private final CollectionUtil collectionUtil;
    private final TypeUtil typeUtil;

    public SimpleParamInfoFactory(final Types types,
                                  final Elements elements,
                                  final ArrayUtil arrayUtil,
                                  final CollectionUtil collectionUtil,
                                  final TypeUtil typeUtil) {
        this.types = types;
        this.elements = elements;
        this.arrayUtil = arrayUtil;
        this.collectionUtil = collectionUtil;
        this.typeUtil = typeUtil;
    }

    public List<ParamInfo> create(final ExecutableElement method) throws InvalidInputParamException {
        final List<? extends VariableElement> params = method.getParameters();
        final List<ParamInfo> paramInfos = new ArrayList<>();
        for (final var param : params) {
            final var collectionType = collectionUtil.getCollectionElementType(param.asType());
            final var arrayType = arrayUtil.getArrayElementType(param.asType());
            if (nonNull(collectionType)) {
                paramInfos.add(buildSimpleParamInfo(method, param, collectionType));
            } else if (nonNull(arrayType)) {
                paramInfos.add(buildSimpleParamInfo(method, param, arrayType));
            } else {
                paramInfos.add(buildSimpleParamInfo(method, param, null));
            }
        }
        return paramInfos;
    }

    private ParamInfo buildSimpleParamInfo(final ExecutableElement method,
                                           final VariableElement param,
                                           @Nullable
                                           final TypeMirror collectionType)
            throws InvalidInputParamException{
        final var paramName = param.getSimpleName().toString();
        final var simpleParamInfoBuilder = SimpleParamInfo.builder()
                .withName(paramName)
                .withType(param.asType())
                .withCustomEnum(typeUtil.isEnum(param.asType()))
                .withContainerType(collectionType);
        return Optional.ofNullable(param.getAnnotation(InputParam.class))
                .map(LambdaUtil.unchecked(i -> {
                    final String convertMethod;
                    TypeMirror enumMethodType = null;
                    if (typeUtil.isEnum(param.asType())) {
                        convertMethod = String.format("%s.%s()", paramName, i.enumMethodValue());
                        final var enumType = (TypeElement) types.asElement(param.asType());
                        enumMethodType = ElementFilter.methodsIn(elements.getAllMembers(enumType)).stream()
                                .filter(m -> m.getSimpleName().contentEquals(i.enumMethodValue()))
                                .findFirst()
                                .orElseThrow(() -> {
                                    final var message = String.format(
                                            "An enum %s does not contains method %s",
                                            enumType.getQualifiedName(),
                                            i.enumMethodValue()
                                    );
                                    return new InvalidInputParamException(message, method);
                                }).getReturnType();
                    } else {
                        convertMethod = i.value().isBlank() ? paramName : i.value();
                    }
                    final var queryParamName = i.statementField().isBlank() ?
                            StringUtil.camelToSnakeCase(paramName) :
                            i.statementField();
                    return simpleParamInfoBuilder.withQueryParamName(queryParamName)
                            .withConvertMethod(convertMethod)
                            .withIgnore(i.ignore())
                            .withEnumMethodType(enumMethodType)
                            .build();
                })).orElseGet(() -> simpleParamInfoBuilder
                            .withQueryParamName(StringUtil.camelToSnakeCase(paramName))
                            .withConvertMethod(paramName)
                            .build()
                );
    }

}
