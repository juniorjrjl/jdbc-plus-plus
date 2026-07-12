package br.com.jdbcpp.processor.dto.parameter;

import org.jspecify.annotations.Nullable;

import javax.lang.model.type.TypeMirror;

public non-sealed class SimpleParamInfo extends ParamInfo {

    private final boolean customEnum;
    private final String queryParamName;

    public SimpleParamInfo(final String name,
                           final TypeMirror type,
                           final boolean customEnum,
                           final String queryParamName,
                           final String convertMethod) {
        super(name, type, null, convertMethod);
        this.customEnum = customEnum;
        this.queryParamName = queryParamName;
    }

    public SimpleParamInfo(final String name,
                           final TypeMirror type,
                           final boolean customEnum,
                           @Nullable
                           final TypeMirror containerType,
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

}
