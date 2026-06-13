package br.com.jdbcpp.processor.dto.parameter;

import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ClassParamInfo extends ParamInfo{

    private final List<ParamInfo> nestedProperties;

    public ClassParamInfo(final String name,
                          final TypeName type,
                          @Nullable
                          final TypeName containerType,
                          final List<ParamInfo> nestedProperties) {
        super(name, type, containerType);
        this.nestedProperties = nestedProperties;
    }

    public List<ParamInfo> getNestedProperties() {
        return nestedProperties;
    }

    @Override
    public boolean isNested() {
        return true;
    }
}
