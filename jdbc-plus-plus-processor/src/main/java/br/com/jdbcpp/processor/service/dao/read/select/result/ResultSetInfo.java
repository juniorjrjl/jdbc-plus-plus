package br.com.jdbcpp.processor.service.dao.read.select.result;

import br.com.jdbcpp.api.ResultBuildStrategyType;
import br.com.jdbcpp.processor.dto.result.ConstructorStrategy;
import br.com.jdbcpp.processor.dto.result.SetterStrategy;
import br.com.jdbcpp.processor.dto.result.SimpleResultStrategy;
import org.jspecify.annotations.Nullable;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public interface ResultSetInfo {

    ResultBuildStrategyType getStrategyType();

    List<ConstructorStrategy> getConstructorStrategies();

    List<SetterStrategy> getSetterStrategies();

    List<SimpleResultStrategy> getSimpleResultStrategies();

    TypeMirror getReturnType();

    @Nullable
    default TypeMirror getContainerReturnTypeMirror(){
        return null;
    }

}
