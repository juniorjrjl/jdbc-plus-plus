package br.com.jdbcpp.processor.dto.method;


import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

public non-sealed class InsertMethod extends MethodInfo{

    private final boolean returnRowsAffected;
    @Nullable
    private final String pkNameOrIndex;
    @Nullable
    private final String customReturnType;

    public InsertMethod(final String name,
                        final TypeMirror returnType,
                        final List<ParamInfo> params,
                        final Map<String, List<ParamInfo>> classPropertyMap,
                        final StatementInfo statement,
                        final TypeMirror packException,
                        final List<? extends AnnotationMirror> annotations,
                        final boolean returnRowsAffected,
                        @Nullable
                        final String pkNameOrIndex,
                        @Nullable
                        final String customReturnType) {
        super(name, returnType, params, classPropertyMap, statement, packException, annotations);
        this.returnRowsAffected = returnRowsAffected;
        this.pkNameOrIndex = pkNameOrIndex;
        this.customReturnType = customReturnType;
    }

    public boolean isReturnRowsAffected() {
        return returnRowsAffected;
    }

    @Nullable
    public String getPkNameOrIndex() {
        return pkNameOrIndex;
    }

    @Nullable
    public String getCustomReturnType() {
        return customReturnType;
    }

    public static class InsertMethodBuilder extends MethodInfo.MethodInfoBuilder {

        @Nullable
        private Boolean returnRowsAffected;
        @Nullable
        private String pkNameOrIndex;
        @Nullable
        private String customReturnType;

        public InsertMethodBuilder(final MethodInfoBuilder baseBuilder) {
            this.name = baseBuilder.name;
            this.returnType = baseBuilder.returnType;
            this.params.addAll(baseBuilder.params);
            this.classPropertyMap.putAll(baseBuilder.classPropertyMap);
            this.statement = baseBuilder.statement;
            this.packException = baseBuilder.packException;
            this.annotations = baseBuilder.annotations;
        }

        public InsertMethodBuilder withReturnRowsAffected(final boolean returnRowsAffected) {
            this.returnRowsAffected = returnRowsAffected;
            return this;
        }

        public  InsertMethodBuilder withPkNameOrIndex(@Nullable final String pkNameOrIndex) {
            this.pkNameOrIndex = pkNameOrIndex;
            return this;
        }

        public InsertMethodBuilder withCustomReturnType(@Nullable final String customReturnType) {
            this.customReturnType = customReturnType;
            return this;
        }

        public InsertMethod build() {
            return new InsertMethod(
                    requireNonNull(name, "name is required"),
                    requireNonNull(returnType, "returnType is required"),
                    params,
                    classPropertyMap,
                    requireNonNull(statement, "statement is required"),
                    requireNonNull(packException, "packException is required"),
                    isNull(annotations) ? Collections.emptyList() : annotations,
                    requireNonNull(returnRowsAffected, "returnRowsAffected is required"),
                    pkNameOrIndex,
                    customReturnType
            );
        }

    }

}
