package br.com.jdbcpp.processor.dto.method;

import br.com.jdbcpp.api.ResultBuildStrategyType;
import br.com.jdbcpp.processor.dto.result.SelectReturnStrategy;

import java.util.List;

public sealed interface SelectMethodInfoBuilder<T extends MethodInfo>
        permits SelectNullableMethodInfo.SelectNullableMethodInfoBuilder,
        SelectOptionalMethodInfo.SelectOptionalMethodInfoBuilder,
        SelectCollectionMethodInfo.SelectCollectionMethodInfoBuilder {

    SelectMethodInfoBuilder<T> withStrategies(final List<SelectReturnStrategy<?>> strategies);

    SelectMethodInfoBuilder<T> withStrategy(final SelectReturnStrategy<?> strategy);

    SelectMethodInfoBuilder<T> withStrategyType(final ResultBuildStrategyType strategyType);

    T build();

}
