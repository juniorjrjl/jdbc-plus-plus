package br.com.jdbcpp.processor.dto.parameter;

import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import java.util.List;

public non-sealed class  ClassParamInfo extends ParamInfo{

    private final List<ParamInfo> nestedProperties;
    private final boolean recordClass;

    public ClassParamInfo(final String name,
                          final TypeName type,
                          @Nullable
                          final TypeName containerType,
                          final List<ParamInfo> nestedProperties,
                          final boolean recordClass) {
        super(name, type, containerType);
        this.nestedProperties = nestedProperties;
        this.recordClass = recordClass;
    }

    public List<ParamInfo> getNestedProperties() {
        return nestedProperties;
    }

    public boolean isRecordClass() {
        return recordClass;
    }

    @Override
    public boolean isNested() {
        return true;
    }

    public boolean containsNested() {
        return !this.nestedProperties.isEmpty();
    }

}
