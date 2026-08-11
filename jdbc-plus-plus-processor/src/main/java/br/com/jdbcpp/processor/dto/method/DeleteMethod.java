package br.com.jdbcpp.processor.dto.method;


import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import org.jspecify.annotations.Nullable;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public non-sealed class DeleteMethod extends MethodInfo{

    private final boolean returnRowsAffected;

    public DeleteMethod(final String name,
                        final TypeMirror returnType,
                        final List<ParamInfo> params,
                        final Map<String, List<ParamInfo>> classPropertyMap,
                        final StatementInfo statement,
                        final TypeMirror packException,
                        final boolean returnRowsAffected) {
        super(name, returnType, params, classPropertyMap, statement, packException);
        this.returnRowsAffected = returnRowsAffected;
    }

    public boolean isReturnRowsAffected() {
        return returnRowsAffected;
    }

    public static class DeleteMethodBuilder extends MethodInfo.MethodInfoBuilder {

        @Nullable
        private Boolean returnRowsAffected;

        public DeleteMethodBuilder(final MethodInfoBuilder baseBuilder) {
            this.name = baseBuilder.name;
            this.returnType = baseBuilder.returnType;
            this.params.addAll(baseBuilder.params);
            this.classPropertyMap.putAll(baseBuilder.classPropertyMap);
            this.statement = baseBuilder.statement;
            this.packException = baseBuilder.packException;
        }

        public DeleteMethodBuilder withReturnRowsAffected(final boolean returnRowsAffected) {
            this.returnRowsAffected = returnRowsAffected;
            return this;
        }

        public DeleteMethod build() {
            return new DeleteMethod(
                    requireNonNull(name, "name is required"),
                    requireNonNull(returnType, "returnType is required"),
                    params,
                    classPropertyMap,
                    requireNonNull(statement, "statement is required"),
                    requireNonNull(packException, "packException is required"),
                    requireNonNull(returnRowsAffected, "returnRowsAffected is required")
            );
        }

    }

}
