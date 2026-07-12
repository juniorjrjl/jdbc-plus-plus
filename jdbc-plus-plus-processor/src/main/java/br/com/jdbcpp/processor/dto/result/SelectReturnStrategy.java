package br.com.jdbcpp.processor.dto.result;

import br.com.jdbcpp.processor.dto.ParamKind;
import org.jspecify.annotations.Nullable;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public abstract class SelectReturnStrategy<T extends  SelectReturnStrategy<T>> {

    private final String name;
    private final TypeMirror type;
    private final ParamKind paramKind;
    private final List<T> nestedValues;
    @Nullable
    private final TypeMirror genericType;
    @Nullable
    private final Integer resultSetIndex;

    protected SelectReturnStrategy(final String name,
                                   final TypeMirror type,
                                   final ParamKind paramKind,
                                   final  List<T> nestedValues,
                                   @Nullable
                                   final TypeMirror genericType,
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

    public TypeMirror getType() {
        return type;
    }

    public ParamKind getParamKind() {
        return paramKind;
    }

    public List<T> getNestedValues() {
        return nestedValues;
    }

    public @Nullable TypeMirror getGenericType() {
        return genericType;
    }

    public @Nullable Integer getResultSetIndex() {
        return resultSetIndex;
    }
}
