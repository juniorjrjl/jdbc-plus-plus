package br.com.jdbcpp.processor.service.read.select.result;

import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import com.palantir.javapoet.MethodSpec;

import static java.util.Objects.isNull;

public final class SelectResultSetDelegator {

    private final SelectResultUsingConstructor constructor;
    private final SelectResultUsingSetter setter;
    private final SelectResultSimpleResult simpleResult;
    private final SelectResultSimpleResultList simpleResultList;

    public SelectResultSetDelegator(final SelectResultUsingConstructor constructor,
                                    final SelectResultUsingSetter setter,
                                    final SelectResultSimpleResult simpleResult,
                                    final SelectResultSimpleResultList simpleResultList) {
        this.constructor = constructor;
        this.setter = setter;
        this.simpleResult = simpleResult;
        this.simpleResultList = simpleResultList;
    }

    public void build(final SelectMethodInfo selectMethodInfo,
                      final String objectResultName,
                      final String resultSetVar,
                      final MethodSpec.Builder builder){
        switch (selectMethodInfo.getStrategyType()){
            case CONSTRUCTOR -> constructor.build(
                    selectMethodInfo.getConstructorStrategies(),
                    objectResultName,
                    selectMethodInfo.getReturnType(),
                    resultSetVar,
                    builder
            );
            case SETTER -> setter.build(
                    selectMethodInfo.getSetterStrategies(),
                    objectResultName,
                    selectMethodInfo.getReturnType(),
                    resultSetVar,
                    builder
            );
            case SIMPLE_RESULT -> {
                if (isNull(selectMethodInfo.getContainerReturnTypeMirror())) {
                    simpleResult.build(
                            selectMethodInfo.getSimpleResultStrategies(),
                            objectResultName,
                            selectMethodInfo.getReturnType(),
                            resultSetVar,
                            builder
                    );
                } else {
                    simpleResultList.build(
                            selectMethodInfo.getSimpleResultStrategies(),
                            resultSetVar,
                            builder
                    );
                }
            }
        }

    }

}
