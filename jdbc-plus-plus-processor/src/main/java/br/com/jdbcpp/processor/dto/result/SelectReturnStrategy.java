package br.com.jdbcpp.processor.dto.result;

import br.com.jdbcpp.processor.dto.ParamKind;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import java.util.List;

public abstract class SelectReturnStrategy<T extends  SelectReturnStrategy<T>> {

    private final String name;
    private final TypeName type;
    private final ParamKind paramKind;
    private final List<T> nestedValues;
    @Nullable
    private final TypeName genericType;
    @Nullable
    private final Integer resultSetIndex;

    protected SelectReturnStrategy(final String name,
                                   final TypeName type,
                                   final ParamKind paramKind,
                                   final  List<T> nestedValues,
                                   @Nullable
                                   final TypeName genericType,
                                   @Nullable
                                   final Integer resultSetIndex) {
        this.name = name;
        this.type = type;
        this.paramKind = paramKind;
        this.nestedValues = nestedValues;
        this.genericType = genericType;
        this.resultSetIndex = resultSetIndex;
    }

    public String getName() {
        return name;
    }

    public TypeName getType() {
        return type;
    }

    public ParamKind getParamKind() {
        return paramKind;
    }

    public List<T> getNestedValues() {
        return nestedValues;
    }

    public @Nullable TypeName getGenericType() {
        return genericType;
    }

    public @Nullable Integer getResultSetIndex() {
        return resultSetIndex;
    }
}
