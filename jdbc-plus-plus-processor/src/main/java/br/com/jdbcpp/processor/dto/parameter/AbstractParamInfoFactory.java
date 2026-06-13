package br.com.jdbcpp.processor.dto.parameter;

import br.com.jdbcpp.api.InputParam;
import br.com.jdbcpp.processor.util.StringUtil;
import br.com.jdbcpp.processor.util.TypeUtil;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Types;
import java.util.Optional;

public abstract sealed class AbstractParamInfoFactory permits ClassParamInfoFactory, SimpleParamInfoFactory {

    protected AbstractParamInfoFactory() {}

    protected static ParamInfo buildSimpleParamInfo(final Types types,
                                                    final VariableElement param,
                                                    @Nullable
                                                    final TypeName collectionType) {
        final var paramName = param.getSimpleName().toString();
        return Optional.ofNullable(param.getAnnotation(InputParam.class))
                .map(i -> {
                    final String convertMethod;
                    if (TypeUtil.isEnum(param.asType(), types)) {
                        convertMethod = switch (i.strategy()){
                            case STRING -> "toString()";
                            case ORDINAL -> "ordinal()";
                            case CUSTOM_METHOD -> i.value();
                        };
                    } else {
                        convertMethod = i.value().isBlank() ? null : i.value();
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
                        StringUtil.camelToSnakeCase(paramName))
                );
    }

}
