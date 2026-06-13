package br.com.jdbcpp.processor.dto.method;


import br.com.jdbcpp.processor.dto.parameter.ParamInfo;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public non-sealed class DeleteMethod extends MethodInfo{

    private final boolean returnRowsAffected;

    public DeleteMethod(final String name,
                        final TypeMirror returnType,
                        final List<ParamInfo> params,
                        final String statement,
                        final boolean returnRowsAffected) {
        super(name, returnType, params, statement);
        this.returnRowsAffected = returnRowsAffected;
    }

    public boolean isReturnRowsAffected() {
        return returnRowsAffected;
    }

}
