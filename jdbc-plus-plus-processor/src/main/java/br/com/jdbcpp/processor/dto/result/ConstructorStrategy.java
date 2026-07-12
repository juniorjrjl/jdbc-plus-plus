package br.com.jdbcpp.processor.dto.result;

import br.com.jdbcpp.processor.dto.ParamKind;
import org.jspecify.annotations.Nullable;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public class ConstructorStrategy extends SelectReturnStrategy<ConstructorStrategy> {

    public ConstructorStrategy(final String name,
                               final TypeMirror type,
                               final ParamKind paramKind,
                               final List<ConstructorStrategy> nestedValues,
                               @Nullable
                               final  TypeMirror genericType,
                               @Nullable
                               final Integer resultSetIndex) {
        super(name, type, paramKind, nestedValues, genericType, resultSetIndex);
    }

}
