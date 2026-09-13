package br.com.jdbcpp.processor.dto.method.customization;

import javax.annotation.Nullable;
import java.util.List;

public record OperationCustomize(
        @Nullable
        String inputMap,
        @Nullable
        String resultSetMap,
        List<InputMapRangeInfo> inputMapRange,
        List<ColumnMapRangeInfo> columnMapRange
) {

    public boolean hasNoneInputMapRange(){
        return inputMapRange.isEmpty();
    }

}
