package br.com.jdbcpp.processor.service.parameter;

import br.com.jdbcpp.api.InputParam;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.exception.InvalidInputParamException;
import br.com.jdbcpp.processor.util.ArrayUtil;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.TypeUtil;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ParameterInfoDelegator {

    private final SimpleParamInfoFactory simpleParamInfoFactory;
    private final ClassParamInfoFactory classParamInfoFactory;
    private final ArrayUtil arrayUtil;
    private final CollectionUtil collectionUtil;
    private final TypeUtil typeUtil;

    public ParameterInfoDelegator(final SimpleParamInfoFactory simpleParamInfoFactory,
                                  final ClassParamInfoFactory classParamInfoFactory,
                                  final ArrayUtil arrayUtil,
                                  final CollectionUtil collectionUtil,
                                  final TypeUtil typeUtil) {
        this.simpleParamInfoFactory = simpleParamInfoFactory;
        this.classParamInfoFactory = classParamInfoFactory;
        this.arrayUtil = arrayUtil;
        this.collectionUtil = collectionUtil;
        this.typeUtil = typeUtil;
    }

    public List<ParamInfo> create(final ExecutableElement method) throws InvalidInputParamException{
        final var methodName = method.getSimpleName().toString();
        final List<? extends VariableElement> params = method.getParameters();
        if (params.isEmpty()) {
            return Collections.emptyList();
        }

        final var classTypesAmount = params.stream()
                .filter(
                        p -> {
                            if (arrayUtil.isArray(p.asType())){
                                return arrayUtil.isArrayOfClass(p.asType());
                            }
                            if (collectionUtil.isCollectionType(p.asType())) {
                                return typeUtil.isCollectionOfClass(p.asType());
                            }
                            return typeUtil.isNotSimpleType(p.asType());
                        }
                )
                .count();

        if (classTypesAmount > 1) {
            final var message = String.format(
                    "A method %s must receive 1 class param or many simple type params",
                    methodName
            );
            throw new InvalidInputParamException(message, method);
        }

        if (classTypesAmount == 1){
            final var param = params.getFirst();
            return classParamInfoFactory.create(param);
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
            throw new InvalidInputParamException(message, method);
        }


        return simpleParamInfoFactory.create(method);
    }

}
