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

    public List<ParamInfo> create(final List<? extends VariableElement> params) {
        final List<ParamInfo> paramInfos = new ArrayList<>();
        for (final var param : params) {
            final var collectionType = collectionUtil.getCollectionElementType(param.asType());
            final var arrayType = arrayUtil.getArrayElementType(param.asType());
            if (nonNull(collectionType)) {
                paramInfos.add(buildSimpleParamInfo(param, collectionType));
            } else if (nonNull(arrayType)) {
                paramInfos.add(buildSimpleParamInfo(param, arrayType));
            } else {
                paramInfos.add(buildSimpleParamInfo(param, null));
            }
        }
        return paramInfos;
    }

    private ParamInfo buildSimpleParamInfo(final VariableElement param,
                                           @Nullable
                                           final TypeMirror collectionType) {
        final var paramName = param.getSimpleName().toString();
        return Optional.ofNullable(param.getAnnotation(InputParam.class))
                .map(i -> {
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
                                    return new InvalidInputParamException(message);
                                }).getReturnType();
                    } else {
                        convertMethod = i.value().isBlank() ? paramName : i.value();
                    }
                    return new SimpleParamInfo(
                            paramName,
                            param.asType(),
                            typeUtil.isEnum(param.asType()),
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
                        typeUtil.isEnum(param.asType()),
                        collectionType,
                        StringUtil.camelToSnakeCase(paramName),
                        paramName,
                        null
                ));
    }

}
