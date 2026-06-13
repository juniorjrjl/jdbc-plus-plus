package br.com.jdbcpp.processor.dto.parameter;

import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

public class SimpleParamInfo extends ParamInfo {

    private final String queryParamName;
    private final @Nullable String convertMethod;

    public SimpleParamInfo(final String name,
                           final TypeName type,
                           final String queryParamName,
                           @Nullable
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
                           @Nullable
                           final String convertMethod) {
        super(name, type, containerType);
        this.queryParamName = queryParamName;
        this.convertMethod = convertMethod;
    }

    public SimpleParamInfo(final String name,
                           final TypeName type,
                           @Nullable
                           final TypeName containerType,
                           final String queryParamName) {
        super(name, type, containerType);
        this.queryParamName = queryParamName;
        this.convertMethod = null;
    }

    public SimpleParamInfo(final String name,
                           final TypeName type,
                           final String queryParamName) {
        super(name, type, null);
        this.queryParamName = queryParamName;
        this.convertMethod = null;
    }

    public String getQueryParamName() {
        return queryParamName;
    }

    public @Nullable String getConvertMethod() {
        return convertMethod;
    }

    @Override
    public boolean isNested() {
        return false;
    }
}
