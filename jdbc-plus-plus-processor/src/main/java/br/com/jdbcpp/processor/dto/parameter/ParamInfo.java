package br.com.jdbcpp.processor.dto.parameter;

import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

public abstract class ParamInfo {

    private final String name;
    private final TypeName type;
    private final TypeName containerType;

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
