package br.com.jdbcpp.processor.dto.method;


import br.com.jdbcpp.processor.dto.method.customization.OperationCustomize;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

public non-sealed class InsertMethod extends MethodInfo {

    @Nullable
    private final String customReturnType;
    private final boolean returnRowsAffected;
    private final List<String> dataNameOrIndex;

    private InsertMethod(final String name,
                         final TypeMirror returnType,
                         final List<ParamInfo> params,
                         final Map<String, List<ParamInfo>> classPropertyMap,
                         final StatementInfo statement,
                         final TypeMirror packException,
                         final List<? extends AnnotationMirror> annotations,
                         @Nullable
                         final OperationCustomize operationCustomize,
                         final boolean returnRowsAffected,
                         final List<String> dataNameOrIndex,
                         @Nullable
                         final String customReturnType) {
        super(name, returnType, params, classPropertyMap, statement, packException, annotations, operationCustomize);
        this.customReturnType = customReturnType;
        this.returnRowsAffected = returnRowsAffected;
        this.dataNameOrIndex = dataNameOrIndex;
    }

    @Nullable
    public String getCustomReturnType() {
        return customReturnType;
    }

    public boolean isReturnRowsAffected() {
        return returnRowsAffected;
    }

    public List<String> getDataNameOrIndex() {
        return dataNameOrIndex;
    }

    public static class InsertMethodBuilder extends MethodInfo.MethodInfoBuilder {

        @Nullable
        protected String customReturnType;
        @Nullable
        private Boolean returnRowsAffected;
        private final List<String> dataNameOrIndex = new ArrayList<>();

        public InsertMethodBuilder(final MethodInfoBuilder baseBuilder) {
            this.name = baseBuilder.name;
            this.returnType = baseBuilder.returnType;
            this.params.addAll(baseBuilder.params);
            this.classPropertyMap.putAll(baseBuilder.classPropertyMap);
            this.statement = baseBuilder.statement;
            this.packException = baseBuilder.packException;
            this.annotations = baseBuilder.annotations;
            this.operationCustomize = baseBuilder.operationCustomize;
        }

        public InsertMethodBuilder withCustomReturnType(@Nullable final String customReturnType) {
            this.customReturnType = customReturnType;
            return this;
        }

        public InsertMethodBuilder withReturnRowsAffected(final boolean returnRowsAffected) {
            this.returnRowsAffected = returnRowsAffected;
            return this;
        }

        public InsertMethodBuilder withDataNameOrIndex(final List<String> dataNameOrIndex) {
            this.dataNameOrIndex.addAll(dataNameOrIndex);
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
                    operationCustomize,
                    requireNonNull(returnRowsAffected, "returnRowsAffected is required"),
                    dataNameOrIndex,
                    customReturnType
            );
        }

    }

}
