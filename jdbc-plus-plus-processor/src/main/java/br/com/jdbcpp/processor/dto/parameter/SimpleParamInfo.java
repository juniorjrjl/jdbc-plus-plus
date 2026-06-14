package br.com.jdbcpp.processor.dto.parameter;

import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

public class SimpleParamInfo extends ParamInfo {

    private final String queryParamName;
    private final String convertMethod;

    public SimpleParamInfo(final String name,
                           final TypeName type,
                           final String queryParamName,
                           final String convertMethod) {
        super(name, type, null);
        this.queryParamName = queryParamName;
        this.convertMethod = convertMethod;
    }

    public SimpleParamInfo(final String name,
                           final TypeName type,
                           @Nullable
                           final TypeName containerType,
                           final String queryParamName,
                           final String convertMethod) {
        super(name, type, containerType);
        this.queryParamName = queryParamName;
        this.convertMethod = convertMethod;
    }

    public String getQueryParamName() {
        return queryParamName;
    }

    public String getConvertMethod() {
        return convertMethod;
    }

    @Override
    public boolean isNested() {
        return false;
    }
}
