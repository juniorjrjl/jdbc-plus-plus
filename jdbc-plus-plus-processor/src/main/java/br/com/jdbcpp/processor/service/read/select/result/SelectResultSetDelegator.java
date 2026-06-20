package br.com.jdbcpp.processor.service.read.select.result;

import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import com.palantir.javapoet.MethodSpec;

public final class SelectResultSetDelegator {

    private final SelectResultUsingConstructor constructor;
    private final SelectResultUsingSetter setter;
    private final SelectResultSimpleResult simpleResult;

    public SelectResultSetDelegator(final SelectResultUsingConstructor constructor,
                                    final SelectResultUsingSetter setter,
                                    final SelectResultSimpleResult simpleResult) {
        this.constructor = constructor;
        this.setter = setter;
        this.simpleResult = simpleResult;
    }

    public void build(final SelectMethodInfo selectMethodInfo,
                                    final String objectResultName,
                                    final String resultSetVar,
                                    final MethodSpec.Builder builder){
        switch (selectMethodInfo.getStrategyType()){
            case CONSTRUCTOR -> constructor.build(
                    selectMethodInfo.getConstructorStrategies(),
                    objectResultName,
                    selectMethodInfo.getReturnTypeMirror(),
                    resultSetVar,
                    builder
            );
            case SETTER -> setter.build(
                    selectMethodInfo.getSetterStrategies(),
                    objectResultName,
                    selectMethodInfo.getReturnTypeMirror(),
                    resultSetVar,
                    builder
            );
            case SIMPLE_RESULT -> simpleResult.build(
                    selectMethodInfo.getSimpleResultStrategies(),
                    objectResultName,
                    selectMethodInfo.getReturnTypeMirror(),
                    resultSetVar,
                    builder
            );
        }

    }

}
