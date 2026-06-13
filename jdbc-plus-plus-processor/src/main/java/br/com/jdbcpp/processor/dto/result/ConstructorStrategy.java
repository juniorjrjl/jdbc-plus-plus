package br.com.jdbcpp.processor.dto.result;

import br.com.jdbcpp.processor.dto.ParamKind;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ConstructorStrategy extends SelectReturnStrategy<ConstructorStrategy> {

    public ConstructorStrategy(final String name,
                               final TypeName type,
                               final ParamKind paramKind,
                               final List<ConstructorStrategy> nestedValues,
                               @Nullable
                               final  TypeName genericType,
                               @Nullable
                               final Integer resultSetIndex) {
        super(name, type, paramKind, nestedValues, genericType, resultSetIndex);
    }

}
