package br.com.jdbcpp.processor.dto.parameter;

import org.jspecify.annotations.Nullable;

import javax.lang.model.type.TypeMirror;

import static java.util.Objects.requireNonNull;

public non-sealed class SimpleParamInfo extends ParamInfo {

    private final boolean customEnum;
    private final String queryParamName;
    private final boolean ignore;
    @Nullable
    private final TypeMirror enumMethodType;

    private SimpleParamInfo(final String name,
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

    public static SimpleParamInfoBuilder builder() {
        return new SimpleParamInfoBuilder();
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

    public static class SimpleParamInfoBuilder {
        @Nullable
        private String name;
        @Nullable
        private TypeMirror type;
        @Nullable
        private Boolean customEnum;
        @Nullable
        private TypeMirror containerType;
        @Nullable
        private String queryParamName;
        @Nullable
        private String convertMethod;
        private boolean ignore = false;
        @Nullable
        private TypeMirror enumMethodType = null;

        public SimpleParamInfoBuilder withName(final String name) {
            this.name = name;
            return this;
        }

        public SimpleParamInfoBuilder withType(final TypeMirror type) {
            this.type = type;
            return this;
        }

        public SimpleParamInfoBuilder withCustomEnum(final Boolean customEnum) {
            this.customEnum = customEnum;
            return this;
        }

        public SimpleParamInfoBuilder withContainerType(@Nullable final TypeMirror containerType) {
            this.containerType = containerType;
            return this;
        }

        public SimpleParamInfoBuilder withQueryParamName(final String queryParamName) {
            this.queryParamName = queryParamName;
            return this;
        }

        public SimpleParamInfoBuilder withConvertMethod(final String convertMethod) {
            this.convertMethod = convertMethod;
            return this;
        }

        public SimpleParamInfoBuilder withIgnore(final boolean ignore) {
            this.ignore = ignore;
            return this;
        }

        public SimpleParamInfoBuilder withEnumMethodType(@Nullable final TypeMirror enumMethodType) {
            this.enumMethodType = enumMethodType;
            return this;
        }

        public SimpleParamInfo build() {
            return new SimpleParamInfo(
                    requireNonNull(name, "name is required"),
                    requireNonNull(type, "type is required"),
                    requireNonNull(customEnum, "customEnum is required"),
                    containerType,
                    requireNonNull(queryParamName, "queryParamName is required"),
                    requireNonNull(convertMethod, "convertMethod is required"),
                    ignore,
                    enumMethodType
            );
        }
    }


}
