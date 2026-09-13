package br.com.jdbcpp.processor.service.method.customization;

import br.com.jdbcpp.api.method.customize.ColumnMapRange;
import br.com.jdbcpp.api.method.customize.InputMap;
import br.com.jdbcpp.api.method.customize.InputMapRange;
import br.com.jdbcpp.api.method.customize.ResultSetMap;
import br.com.jdbcpp.processor.dto.method.customization.ColumnMapRangeInfo;
import br.com.jdbcpp.processor.dto.method.customization.InputMapRangeInfo;
import br.com.jdbcpp.processor.dto.method.customization.OperationCustomize;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.ExecutableElement;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

import static java.util.Objects.isNull;

public final class CustomizationFactory {

    private CustomizationFactory() {}

    @Nullable
    public static OperationCustomize create(final ExecutableElement method){
        final var resultSetMapper = Optional.ofNullable(method.getAnnotation(ResultSetMap.class))
                .map(ResultSetMap::value)
                .orElse(null);

        final var inputMap = Optional.ofNullable(method.getAnnotation(InputMap.class))
                .map(InputMap::value)
                .orElse(null);

        final var inputMapRanges = Arrays.stream(method.getAnnotationsByType(InputMapRange.class))
                .map(i -> new InputMapRangeInfo(i.value(), i.start(), i.end()))
                .sorted(Comparator.comparingInt(InputMapRangeInfo::start))
                .toList();

        final var columnMapRanges = Arrays.stream(method.getAnnotationsByType(ColumnMapRange.class))
                .map(i -> new ColumnMapRangeInfo(i.value(), i.start(), i.end()))
                .sorted(Comparator.comparingInt(ColumnMapRangeInfo::start))
                .toList();

        return isNull(resultSetMapper) && isNull(inputMap) && inputMapRanges.isEmpty() && columnMapRanges.isEmpty() ?
                null :
                new OperationCustomize(
                        inputMap,
                        resultSetMapper,
                        inputMapRanges,
                        columnMapRanges
                );
    }

}
