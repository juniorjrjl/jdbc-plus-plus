package br.com.jdbcpp.processor.dto.method;

import br.com.jdbcpp.api.ResultBuildStrategyType;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.result.ConstructorStrategy;
import br.com.jdbcpp.processor.dto.result.SelectReturnStrategy;
import br.com.jdbcpp.processor.dto.result.SetterStrategy;
import br.com.jdbcpp.processor.dto.result.SimpleResultStrategy;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import org.jspecify.annotations.Nullable;

import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public non-sealed class SelectMethodInfo extends MethodInfo {

    private final List<SelectReturnStrategy<?>> strategies;
    private final ResultBuildStrategyType strategyType;
    @Nullable
    private final TypeMirror containerReturnTypeMirror;
    @Nullable
    private final TypeMirror instanceContainer;

    private SelectMethodInfo(final String name,
                             final TypeMirror returnType,
                             final List<ParamInfo> params,
                             final Map<String, List<ParamInfo>> classPropertyMap,
                             final StatementInfo statement,
                             final TypeMirror packException,
                             final List<SelectReturnStrategy<?>> strategies,
                             final ResultBuildStrategyType strategyType,
                             @Nullable
                             final TypeMirror containerReturnTypeMirror,
                             @Nullable
                             final TypeMirror instanceContainer) {
        super(name, returnType, params, classPropertyMap, statement, packException);
        this.strategies = strategies;
        this.strategyType = strategyType;
        this.containerReturnTypeMirror = containerReturnTypeMirror;
        this.instanceContainer = instanceContainer;
    }

    public ResultBuildStrategyType getStrategyType() {
        return strategyType;
    }

    @SuppressWarnings("unchecked")
    public List<ConstructorStrategy> getConstructorStrategies() {
        if (strategyType != ResultBuildStrategyType.CONSTRUCTOR) {
            return List.of();
        }
        return (List<ConstructorStrategy>) (List<?>) strategies;
    }

    @SuppressWarnings("unchecked")
    public List<SetterStrategy> getSetterStrategies() {
        if (strategyType != ResultBuildStrategyType.SETTER) {
            return List.of();
        }
        return (List<SetterStrategy>) (List<?>) strategies;
    }

    @SuppressWarnings("unchecked")
    public List<SimpleResultStrategy> getSimpleResultStrategies() {
        if (strategyType != ResultBuildStrategyType.SIMPLE_RESULT) {
            return List.of();
        }
        return (List<SimpleResultStrategy>) (List<?>) strategies;
    }

    @Nullable
    public TypeMirror getContainerReturnTypeMirror() {
        return containerReturnTypeMirror;
    }

    public @Nullable TypeMirror getInstanceContainer() {
        return instanceContainer;
    }

    public static class SelectMethodInfoBuilder extends MethodInfo.MethodInfoBuilder {

        private final List<SelectReturnStrategy<?>> strategies = new ArrayList<>();
        @Nullable
        private ResultBuildStrategyType strategyType;
        @Nullable
        private TypeMirror containerReturnTypeMirror;
        @Nullable
        private TypeMirror instanceContainer;

        public SelectMethodInfoBuilder(final MethodInfoBuilder baseBuilder) {
            this.name = baseBuilder.name;
            this.returnType = baseBuilder.returnType;
            this.params.addAll(baseBuilder.params);
            this.classPropertyMap.putAll(baseBuilder.classPropertyMap);
            this.statement = baseBuilder.statement;
            this.packException = baseBuilder.packException;
        }

        public SelectMethodInfoBuilder withStrategies(final List<SelectReturnStrategy<?>> strategies) {
            this.strategies.addAll(strategies);
            return this;
        }

        public SelectMethodInfoBuilder withStrategy(final SelectReturnStrategy<?> strategy) {
            this.strategies.add(strategy);
            return this;
        }

        public  SelectMethodInfoBuilder withStrategyType(final ResultBuildStrategyType strategyType) {
            this.strategyType = strategyType;
            return this;
        }

        public  SelectMethodInfoBuilder withContainerReturnTypeMirror(@Nullable final TypeMirror containerReturnTypeMirror) {
            this.containerReturnTypeMirror = containerReturnTypeMirror;
            return this;
        }

        public  SelectMethodInfoBuilder withInstanceContainer(@Nullable final TypeMirror instanceContainer) {
            this.instanceContainer = instanceContainer;
            return this;
        }

        public SelectMethodInfo build() {
            return new SelectMethodInfo(
                    requireNonNull(name, "name is required"),
                    requireNonNull(returnType, "returnType is required"),
                    params,
                    classPropertyMap,
                    requireNonNull(statement, "statement is required"),
                    requireNonNull(packException, "packException is required"),
                    strategies,
                    requireNonNull(strategyType, "strategyType is required"),
                    containerReturnTypeMirror,
                    instanceContainer
            );
        }

    }

}
