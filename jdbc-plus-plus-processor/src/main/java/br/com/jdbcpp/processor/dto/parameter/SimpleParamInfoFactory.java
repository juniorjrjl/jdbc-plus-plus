package br.com.jdbcpp.processor.dto.parameter;

import br.com.jdbcpp.processor.util.ArrayUtil;
import br.com.jdbcpp.processor.util.CollectionUtil;
import com.palantir.javapoet.TypeName;

import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

public final class SimpleParamInfoFactory extends AbstractParamInfoFactory {

    private SimpleParamInfoFactory() {}

    public static List<ParamInfo> create(final List<? extends VariableElement> params,
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

}
