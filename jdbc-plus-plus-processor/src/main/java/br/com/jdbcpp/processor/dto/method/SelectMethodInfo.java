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
import java.util.List;
import java.util.Map;

import static br.com.jdbcpp.api.ResultBuildStrategyType.SIMPLE_RESULT;

public non-sealed class SelectMethodInfo extends MethodInfo {

    private final List<SelectReturnStrategy<?>> strategies;
    private final ResultBuildStrategyType strategyType;
    @Nullable
    private final TypeMirror containerReturnTypeMirror;
    @Nullable
    private final TypeMirror instanceContainer;

    public SelectMethodInfo(final String name,
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

    public SelectMethodInfo(final String name,
                            final TypeMirror returnType,
                            final List<ParamInfo> params,
                            final Map<String, List<ParamInfo>> classPropertyMap,
                            final StatementInfo statement,
                            @Nullable
                            final TypeMirror packException,
                            final SelectReturnStrategy<?> strategy,
                            @Nullable
                            final TypeMirror containerReturnTypeMirror,
                            @Nullable
                            final TypeMirror instanceContainer){
        super(name, returnType, params, classPropertyMap, statement, packException);
        this.strategies = List.of(strategy);
        this.strategyType = SIMPLE_RESULT;
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
}
