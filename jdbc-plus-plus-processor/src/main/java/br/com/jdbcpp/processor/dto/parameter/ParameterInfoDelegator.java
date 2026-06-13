package br.com.jdbcpp.processor.dto.parameter;

import br.com.jdbcpp.api.InputParam;
import br.com.jdbcpp.processor.exception.InvalidInputParamException;
import br.com.jdbcpp.processor.util.ArrayUtil;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.TypeUtil;

import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Objects;

public final class ParameterInfoDelegator {

    private ParameterInfoDelegator() {}

    public static List<ParamInfo> create(final String methodName,
                                         final List<? extends VariableElement> params,
                                         final Types types){
        if (params.isEmpty()) {
            final var message = String.format("Method '%s' must have at least one parameter", methodName);
            throw new InvalidInputParamException(message);
        }

        final var classTypesAmount = params.stream()
                .filter(
                        p -> TypeUtil.isNotSimpleType(p.asType(), types) ||
                                CollectionUtil.isCollectionOfClass(p.asType(), types) ||
                                ArrayUtil.isArrayOfClass(p.asType(), types)
                )
                .count();

        if (classTypesAmount > 1) {
            final var message = String.format(
                    "A method %s must receive 1 class param or many simple type params",
                    methodName
            );
            throw new InvalidInputParamException(message);
        }

        if (classTypesAmount == 1){
            final var param = params.getFirst();
            return ClassParamInfoFactory.create(param, types);
        }

        if ((params.stream()
                .map(p -> p.getAnnotation(InputParam.class))
                .filter(Objects::nonNull)
                .anyMatch(InputParam::ignore))) {
            final var message = String.format(
                    "Invalid configuration in method '%s': The '@InputParam(ignore = true)' annotation " +
                            "can only be used on class properties, not on direct method parameters.",
                    methodName
            );
            throw new InvalidInputParamException(message);
        }


        return SimpleParamInfoFactory.create(params, types);
    }

}
