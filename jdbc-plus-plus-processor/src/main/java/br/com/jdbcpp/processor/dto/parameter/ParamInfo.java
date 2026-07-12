package br.com.jdbcpp.processor.dto.parameter;

import org.jspecify.annotations.Nullable;

import javax.lang.model.type.TypeMirror;

import static java.util.Objects.nonNull;

public abstract sealed class ParamInfo permits SimpleParamInfo, ClassParamInfo{

    protected final String name;
    protected final TypeMirror type;
    @Nullable
    protected final TypeMirror containerType;
    private final String convertMethod;

    protected ParamInfo(final String name,
                        final TypeMirror type,
                        @Nullable
                        final TypeMirror containerType,
                        final String convertMethod) {
        this.name = name;
        this.type = type;
        this.containerType = containerType;
        this.convertMethod = convertMethod;
    }

    public String getName() {
        return name;
    }

    public TypeMirror getType() {
        return type;
    }

    @Nullable
    public TypeMirror getContainerType() {
        return containerType;
    }

    public String getConvertMethod() {
        return convertMethod;
    }

    public abstract boolean isNested();

    public boolean hasContainer(){
        return nonNull(containerType);
    }

}
