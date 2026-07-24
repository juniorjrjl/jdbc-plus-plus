package br.com.jdbcpp.processor.dto.method;


import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import org.jspecify.annotations.Nullable;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Map;

public non-sealed class InsertMethod extends MethodInfo{

    private final boolean returnRowsAffected;

    public InsertMethod(final String name,
                        final TypeMirror returnType,
                        final List<ParamInfo> params,
                        final Map<String, List<ParamInfo>> classPropertyMap,
                        final StatementInfo statement,
                        @Nullable
                        final TypeMirror packException,
                        final boolean returnRowsAffected) {
        super(name, returnType, params, classPropertyMap, statement, packException);
        this.returnRowsAffected = returnRowsAffected;
    }

    public boolean isReturnRowsAffected() {
        return returnRowsAffected;
    }

}
