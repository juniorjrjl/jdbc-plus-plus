package br.com.jdbcpp.processor.dto.parameter;

import br.com.jdbcpp.api.InputParam;
import br.com.jdbcpp.processor.exception.InvalidInputParamException;
import br.com.jdbcpp.processor.util.ArrayUtil;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.StringUtil;
import br.com.jdbcpp.processor.util.TypeUtil;
import org.jspecify.annotations.Nullable;

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

    public List<ParamInfo> create(final List<? extends VariableElement> params,
                                  final Types types,
                                  final Elements elements) {
        final List<ParamInfo> paramInfos = new ArrayList<>();
        for (final var param : params) {
            final var collectionType = CollectionUtil.getCollectionElementType(param.asType());
            final var arrayType = ArrayUtil.getArrayElementType(param.asType());
            if (nonNull(collectionType)) {
                paramInfos.add(buildSimpleParamInfo(types, elements, param, collectionType));
            } else if (nonNull(arrayType)) {
                paramInfos.add(buildSimpleParamInfo(types, elements, param, arrayType));
            } else {
                paramInfos.add(buildSimpleParamInfo(types, elements, param, null));
            }
        }
        return paramInfos;
    }

    private static ParamInfo buildSimpleParamInfo(final Types types,
                                                  final Elements elements,
                                                  final VariableElement param,
                                                  @Nullable
                                                  final TypeMirror collectionType) {
        final var paramName = param.getSimpleName().toString();
        return Optional.ofNullable(param.getAnnotation(InputParam.class))
                .map(i -> {
                    final String convertMethod;
                    TypeMirror enumMethodType = null;
                    if (TypeUtil.isEnum(param.asType(), types)) {
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
                                    return new InvalidInputParamException(message);
                                }).getReturnType();
                    } else {
                        convertMethod = i.value().isBlank() ? paramName : i.value();
                    }
                    return new SimpleParamInfo(
                            paramName,
                            param.asType(),
                            TypeUtil.isEnum(param.asType(), types),
                            collectionType,
                            i.statementField().isBlank() ?
                                    StringUtil.camelToSnakeCase(paramName) :
                                    i.statementField(),
                            convertMethod,
                            i.ignore(),
                            enumMethodType
                    );
                }).orElseGet(() -> new SimpleParamInfo(
                        paramName,
                        param.asType(),
                        TypeUtil.isEnum(param.asType(), types),
                        collectionType,
                        StringUtil.camelToSnakeCase(paramName),
                        paramName,
                        null
                ));
    }

}
