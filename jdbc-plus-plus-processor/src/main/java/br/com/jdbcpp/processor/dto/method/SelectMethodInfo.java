package br.com.jdbcpp.processor.dto.method;

import br.com.jdbcpp.api.ResultBuildStrategyType;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.result.ConstructorStrategy;
import br.com.jdbcpp.processor.dto.result.SelectReturnStrategy;
import br.com.jdbcpp.processor.dto.result.SetterStrategy;
import br.com.jdbcpp.processor.dto.result.SimpleResultStrategy;

import javax.lang.model.type.TypeMirror;
import java.util.List;

import static br.com.jdbcpp.api.ResultBuildStrategyType.SIMPLE_RESULT;

public non-sealed class SelectMethodInfo extends MethodInfo {

    private final List<SelectReturnStrategy<?>> strategies;
    private final ResultBuildStrategyType strategyType;
    private final TypeMirror returnTypeMirror;

    public SelectMethodInfo(final String name,
                            final TypeMirror returnType,
                            final List<ParamInfo> params,
                            final String statement,
                            final List<SelectReturnStrategy<?>> strategies,
                            final ResultBuildStrategyType strategyType) {
        super(name, returnType, params, statement);
        this.returnTypeMirror = returnType;
        this.strategies = strategies;
        this.strategyType = strategyType;
    }

    public SelectMethodInfo(final String name,
                            final TypeMirror returnType,
                            final List<ParamInfo> params,
                            final String statement,
                            final SelectReturnStrategy<?> strategy){
        super(name, returnType, params, statement);
        this.returnTypeMirror = returnType;
        this.strategies = List.of(strategy);
        this.strategyType = SIMPLE_RESULT;
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

    public TypeMirror getReturnTypeMirror() {
        return returnTypeMirror;
    }
}
