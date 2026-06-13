package br.com.jdbcpp.processor.dto.method;

import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import com.palantir.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public abstract sealed class MethodInfo permits DeleteMethod, InsertMethod, SelectMethodInfo, UpdateMethod {

    protected final String name;
    protected final TypeName returnType;
    protected final List<ParamInfo> params;
    protected final String statement;

    protected MethodInfo(final String name,
                      final TypeMirror returnType,
                      final List<ParamInfo> params,
                      final String statement) {
        this.name = name;
        this.returnType = TypeName.get(returnType);
        this.params = params;
        this.statement = statement;
    }

    public String getName() {
        return name;
    }

    public TypeName getReturnType() {
        return returnType;
    }

    public List<ParamInfo> getParams() {
        return params;
    }

    public String getStatement() {
        return statement;
    }

}
