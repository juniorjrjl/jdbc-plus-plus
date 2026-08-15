package br.com.jdbcpp.processor.dto.method;

import br.com.jdbcpp.api.ResultBuildStrategyType;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.result.ConstructorStrategy;
import br.com.jdbcpp.processor.dto.result.SelectReturnStrategy;
import br.com.jdbcpp.processor.dto.result.SetterStrategy;
import br.com.jdbcpp.processor.dto.result.SimpleResultStrategy;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import br.com.jdbcpp.processor.service.dao.read.select.result.ResultSetInfo;
import org.jspecify.annotations.Nullable;

import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public non-sealed class SelectCollectionMethodInfo extends MethodInfo implements ResultSetInfo {

    private final List<SelectReturnStrategy<?>> strategies;
    private final ResultBuildStrategyType strategyType;
    private final TypeMirror containerReturnTypeMirror;
    private final TypeMirror instanceContainer;

    private SelectCollectionMethodInfo(final String name,
                                       final TypeMirror returnType,
                                       final List<ParamInfo> params,
                                       final Map<String, List<ParamInfo>> classPropertyMap,
                                       final StatementInfo statement,
                                       final TypeMirror packException,
                                       final List<SelectReturnStrategy<?>> strategies,
                                       final ResultBuildStrategyType strategyType,
                                       final TypeMirror containerReturnTypeMirror,
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

    public TypeMirror getContainerReturnTypeMirror() {
        return containerReturnTypeMirror;
    }

    public TypeMirror getInstanceContainer() {
        return instanceContainer;
    }

    public static non-sealed class SelectCollectionMethodInfoBuilder extends MethodInfoBuilder
            implements SelectMethodInfoBuilder<SelectCollectionMethodInfo> {

        private final List<SelectReturnStrategy<?>> strategies = new ArrayList<>();
        @Nullable
        private ResultBuildStrategyType strategyType;
        @Nullable
        private TypeMirror containerReturnTypeMirror;
        @Nullable
        private TypeMirror instanceContainer;

        public SelectCollectionMethodInfoBuilder(final MethodInfoBuilder baseBuilder) {
            this.name = baseBuilder.name;
            this.returnType = baseBuilder.returnType;
            this.params.addAll(baseBuilder.params);
            this.classPropertyMap.putAll(baseBuilder.classPropertyMap);
            this.statement = baseBuilder.statement;
            this.packException = baseBuilder.packException;
        }

        @Override
        public SelectCollectionMethodInfoBuilder withStrategies(final List<SelectReturnStrategy<?>> strategies) {
            this.strategies.addAll(strategies);
            return this;
        }

        @Override
        public SelectCollectionMethodInfoBuilder withStrategy(final SelectReturnStrategy<?> strategy) {
            this.strategies.add(strategy);
            return this;
        }

        @Override
        public SelectCollectionMethodInfoBuilder withStrategyType(final ResultBuildStrategyType strategyType) {
            this.strategyType = strategyType;
            return this;
        }

        public SelectCollectionMethodInfoBuilder withContainerReturnTypeMirror(final TypeMirror containerReturnTypeMirror) {
            this.containerReturnTypeMirror = containerReturnTypeMirror;
            return this;
        }

        public SelectCollectionMethodInfoBuilder withInstanceContainer(final TypeMirror instanceContainer) {
            this.instanceContainer = instanceContainer;
            return this;
        }

        public SelectCollectionMethodInfo build() {
            return new SelectCollectionMethodInfo(
                    requireNonNull(name, "name is required"),
                    requireNonNull(returnType, "returnType is required"),
                    params,
                    classPropertyMap,
                    requireNonNull(statement, "statement is required"),
                    requireNonNull(packException, "packException is required"),
                    strategies,
                    requireNonNull(strategyType, "strategyType is required"),
                    requireNonNull(containerReturnTypeMirror, "containerReturnTypeMirror is required"),
                    requireNonNull(instanceContainer, "instanceContainer is required")
            );
        }

    }

}
