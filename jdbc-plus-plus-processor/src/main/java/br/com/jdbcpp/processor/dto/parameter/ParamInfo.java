package br.com.jdbcpp.processor.dto.parameter;

import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

public abstract sealed class ParamInfo permits SimpleParamInfo, ClassParamInfo{

    protected final String name;
    protected final TypeName type;
    @Nullable
    protected final TypeName containerType;

    protected ParamInfo(final String name,
                        final TypeName type,
                        @Nullable
                        final TypeName containerType) {
        this.name = name;
        this.type = type;
        this.containerType = containerType;
    }

    public String getName() {
        return name;
    }

    public TypeName getType() {
        return type;
    }

    @Nullable
    public TypeName getContainerType() {
        return containerType;
    }

    public abstract boolean isNested();

}
