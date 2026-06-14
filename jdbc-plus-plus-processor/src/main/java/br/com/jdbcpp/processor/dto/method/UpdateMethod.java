package br.com.jdbcpp.processor.dto.method;


import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public non-sealed class UpdateMethod extends MethodInfo{

    private final boolean returnRowsAffected;

    public UpdateMethod(final String name,
                        final TypeMirror returnType,
                        final List<ParamInfo> params,
                        final StatementInfo statement,
                        final boolean returnRowsAffected) {
        super(name, returnType, params, statement);
        this.returnRowsAffected = returnRowsAffected;
    }

    public boolean isReturnRowsAffected() {
        return returnRowsAffected;
    }

}
