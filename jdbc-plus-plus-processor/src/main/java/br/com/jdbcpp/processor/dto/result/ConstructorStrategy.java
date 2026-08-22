package br.com.jdbcpp.processor.dto.result;

import br.com.jdbcpp.processor.dto.ParamKind;
import org.jspecify.annotations.Nullable;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public class ConstructorStrategy extends SelectReturnStrategy<ConstructorStrategy> {

    @Nullable
    private final String customReturnType;

    public ConstructorStrategy(final String name,
                               final TypeMirror type,
                               final ParamKind paramKind,
                               final List<ConstructorStrategy> nestedValues,
                               @Nullable
                               final  TypeMirror genericType,
                               @Nullable
                               final Integer resultSetIndex,
                               @Nullable
                               final String customReturnType) {
        super(name, type, paramKind, nestedValues, genericType, resultSetIndex);
        this.customReturnType = customReturnType;
    }

    public @Nullable String getCustomReturnType() {
        return customReturnType;
    }

}
