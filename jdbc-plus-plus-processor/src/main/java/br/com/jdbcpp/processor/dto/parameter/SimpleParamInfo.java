package br.com.jdbcpp.processor.dto.parameter;

import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.nonNull;

public non-sealed class SimpleParamInfo extends ParamInfo {

    private final boolean customEnum;
    private final String queryParamName;

    public SimpleParamInfo(final String name,
                           final TypeName type,
                           final boolean customEnum,
                           final String queryParamName,
                           final String convertMethod) {
        super(name, type, null, convertMethod);
        this.customEnum = customEnum;
        this.queryParamName = queryParamName;
    }

    public SimpleParamInfo(final String name,
                           final TypeName type,
                           final boolean customEnum,
                           @Nullable
                           final TypeName containerType,
                           final String queryParamName,
                           final String convertMethod) {
        super(name, type, containerType, convertMethod);
        this.customEnum = customEnum;
        this.queryParamName = queryParamName;
    }

    public boolean isCustomEnum() {
        return customEnum;
    }

    public String getQueryParamName() {
        return queryParamName;
    }

    @Override
    public boolean isNested() {
        return false;
    }

    public boolean hasContainer(){
        return nonNull(containerType);
    }

}
