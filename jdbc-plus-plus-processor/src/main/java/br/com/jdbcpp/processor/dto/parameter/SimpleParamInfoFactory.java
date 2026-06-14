package br.com.jdbcpp.processor.dto.parameter;

import br.com.jdbcpp.api.InputParam;
import br.com.jdbcpp.processor.util.ArrayUtil;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.StringUtil;
import br.com.jdbcpp.processor.util.TypeUtil;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;

public final class SimpleParamInfoFactory {

    public List<ParamInfo> create(final List<? extends VariableElement> params,
                                  final Types types) {
        final List<ParamInfo> paramInfos = new ArrayList<>();
        for (final var param : params) {
            final var collectionType = CollectionUtil.getCollectionElementType(param.asType());
            final var arrayType = ArrayUtil.getArrayElementType(param.asType());
            if (nonNull(collectionType)) {
                paramInfos.add(buildSimpleParamInfo(types, param, TypeName.get(collectionType)));
            } else if (nonNull(arrayType)) {
                paramInfos.add(buildSimpleParamInfo(types, param, TypeName.get(arrayType)));
            } else {
                paramInfos.add(buildSimpleParamInfo(types, param, null));
            }
        }
        return paramInfos;
    }

    private static ParamInfo buildSimpleParamInfo(final Types types,
                                                  final VariableElement param,
                                                  @Nullable
                                                  final TypeName collectionType) {
        final var paramName = param.getSimpleName().toString();
        return Optional.ofNullable(param.getAnnotation(InputParam.class))
                .map(i -> {
                    final String convertMethod;
                    if (TypeUtil.isEnum(param.asType(), types)) {
                        convertMethod = switch (i.strategy()){
                            case STRING -> String.format("%s.toString()", paramName);
                            case ORDINAL -> String.format("%s.ordinal()", paramName);
                            case CUSTOM_METHOD -> String.format("%s.%s", paramName, i.strategy());
                        };
                    } else {
                        convertMethod = i.value().isBlank() ? paramName : i.value();
                    }
                    return new SimpleParamInfo(
                            paramName,
                            TypeName.get(param.asType()),
                            collectionType,
                            i.statementField().isBlank() ?
                                    StringUtil.camelToSnakeCase(paramName) :
                                    i.statementField(),
                            convertMethod
                    );
                }).orElseGet(() -> new SimpleParamInfo(
                        paramName,
                        TypeName.get(param.asType()),
                        collectionType,
                        StringUtil.camelToSnakeCase(paramName),
                        paramName
                ));
    }

}
