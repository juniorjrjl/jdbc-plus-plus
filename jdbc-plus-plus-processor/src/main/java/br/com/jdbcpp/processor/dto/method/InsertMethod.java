package br.com.jdbcpp.processor.dto.method;


import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;

import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public non-sealed class InsertMethod extends MethodInfo{

    private final boolean returnRowsAffected;

    public InsertMethod(final String name,
                        final TypeMirror returnType,
                        final List<ParamInfo> params,
                        final StatementInfo statement,
                        final boolean returnRowsAffected) {
        super(name, returnType, params, Collections.emptyMap(), statement);
        this.returnRowsAffected = returnRowsAffected;
    }

    public InsertMethod(final String name,
                        final TypeMirror returnType,
                        final Map<String, List<ParamInfo>> classPropertyMap,
                        final StatementInfo statement,
                        final boolean returnRowsAffected) {
        super(name, returnType, Collections.emptyList(), classPropertyMap, statement);
        this.returnRowsAffected = returnRowsAffected;
    }

    public boolean isReturnRowsAffected() {
        return returnRowsAffected;
    }

}
