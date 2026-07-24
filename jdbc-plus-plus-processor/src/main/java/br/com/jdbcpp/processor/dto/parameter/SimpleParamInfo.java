package br.com.jdbcpp.processor.dto.parameter;

import org.jspecify.annotations.Nullable;

import javax.lang.model.type.TypeMirror;

public non-sealed class SimpleParamInfo extends ParamInfo {

    private final boolean customEnum;
    private final String queryParamName;
    private final boolean ignore;
    @Nullable
    private final TypeMirror enumMethodType;

    public SimpleParamInfo(final String name,
                           final TypeMirror type,
                           final boolean customEnum,
                           @Nullable
                           final TypeMirror containerType,
                           final String queryParamName,
                           final String convertMethod,
                           @Nullable
                           final TypeMirror enumMethodType) {
        super(name, type, containerType, convertMethod);
        this.customEnum = customEnum;
        this.queryParamName = queryParamName;
        this.enumMethodType = enumMethodType;
        this.ignore = false;
    }

    public SimpleParamInfo(final String name,
                           final TypeMirror type,
                           final boolean customEnum,
                           @Nullable
                           final TypeMirror containerType,
                           final String queryParamName,
                           final String convertMethod,
                           final boolean ignore,
                           @Nullable
                           final TypeMirror enumMethodType) {
        super(name, type, containerType, convertMethod);
        this.customEnum = customEnum;
        this.queryParamName = queryParamName;
        this.enumMethodType = enumMethodType;
        this.ignore = ignore;
    }

    public boolean isCustomEnum() {
        return customEnum;
    }

    public String getQueryParamName() {
        return queryParamName;
    }

    public  boolean isIgnore() {
        return ignore;
    }

    public @Nullable TypeMirror getEnumMethodType() {
        return enumMethodType;
    }

    @Override
    public boolean isNested() {
        return false;
    }

}
