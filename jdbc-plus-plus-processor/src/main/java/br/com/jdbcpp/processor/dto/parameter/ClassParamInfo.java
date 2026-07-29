package br.com.jdbcpp.processor.dto.parameter;

import org.jspecify.annotations.Nullable;

import javax.lang.model.type.TypeMirror;
import java.util.List;

import static java.util.Objects.requireNonNull;

public non-sealed class ClassParamInfo extends ParamInfo {

    private final List<ParamInfo> nestedProperties;
    private final boolean recordClass;

    private ClassParamInfo(final String name,
                           final TypeMirror type,
                           @Nullable
                           final TypeMirror containerType,
                           final List<ParamInfo> nestedProperties,
                           final boolean recordClass,
                           final String convertMethod) {
        super(name, type, containerType, convertMethod);
        this.nestedProperties = nestedProperties;
        this.recordClass = recordClass;
    }

    public static ClassParamInfoBuilder builder(){
        return new ClassParamInfoBuilder();
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

    public static class ClassParamInfoBuilder {
        @Nullable
        private String name;
        @Nullable
        private TypeMirror type;
        @Nullable
        private TypeMirror containerType;
        @Nullable
        private List<ParamInfo> nestedProperties;
        @Nullable
        private Boolean recordClass;
        @Nullable
        private String convertMethod;

        public ClassParamInfoBuilder withName(final String name) {
            this.name = name;
            return this;
        }

        public ClassParamInfoBuilder withType(final TypeMirror type) {
            this.type = type;
            return this;
        }

        public ClassParamInfoBuilder withContainerType(@Nullable final TypeMirror containerType) {
            this.containerType = containerType;
            return this;
        }

        public ClassParamInfoBuilder withNestedProperties(final List<ParamInfo> nestedProperties) {
            this.nestedProperties = nestedProperties;
            return this;
        }

        public ClassParamInfoBuilder withRecordClass(final Boolean recordClass) {
            this.recordClass = recordClass;
            return this;
        }

        public ClassParamInfoBuilder withConvertMethod(final String convertMethod) {
            this.convertMethod = convertMethod;
            return this;
        }

        public ClassParamInfo build() {
            return new ClassParamInfo(
                    requireNonNull(name, "name is required"),
                    requireNonNull(type, "type is required"),
                    containerType,
                    requireNonNull(nestedProperties, "nestedProperties is required"),
                    requireNonNull(recordClass, "recordClass is required"),
                    requireNonNull(convertMethod, "convertMethod is required")
            );
        }
    }

}
