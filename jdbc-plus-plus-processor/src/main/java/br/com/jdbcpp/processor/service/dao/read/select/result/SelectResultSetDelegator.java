package br.com.jdbcpp.processor.service.dao.read.select.result;

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

    public void build(final ResultSetInfo resultSetInfo,
                      final String objectResultName,
                      final String resultSetVar,
                      final MethodSpec.Builder builder){
        switch (resultSetInfo.getStrategyType()){
            case CONSTRUCTOR -> constructor.build(
                    resultSetInfo.getConstructorStrategies(),
                    objectResultName,
                    resultSetInfo.getReturnType(),
                    resultSetVar,
                    builder
            );
            case SETTER -> setter.build(
                    resultSetInfo.getSetterStrategies(),
                    objectResultName,
                    resultSetInfo.getReturnType(),
                    resultSetVar,
                    builder
            );
            case SIMPLE_RESULT -> {
                if (isNull(resultSetInfo.getContainerReturnTypeMirror())) {
                    simpleResult.build(
                            resultSetInfo.getSimpleResultStrategies(),
                            objectResultName,
                            resultSetInfo.getReturnType(),
                            resultSetVar,
                            builder
                    );
                } else {
                    simpleResultList.build(
                            resultSetInfo.getSimpleResultStrategies(),
                            resultSetVar,
                            builder
                    );
                }
            }
        }

    }

}
