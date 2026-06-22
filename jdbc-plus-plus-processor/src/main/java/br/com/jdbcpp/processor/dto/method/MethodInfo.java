package br.com.jdbcpp.processor.dto.method;

import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import com.palantir.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Map;

public abstract sealed class MethodInfo permits DeleteMethod, InsertMethod, SelectMethodInfo, UpdateMethod {

    protected final String name;
    protected final TypeName returnType;
    protected final List<ParamInfo> params;
    protected final Map<String, List<ParamInfo>> classPropertyMap;
    protected final StatementInfo statement;

    protected MethodInfo(final String name,
                         final TypeMirror returnType,
                         final List<ParamInfo> params,
                         final Map<String, List<ParamInfo>> classPropertyMap,
                         final StatementInfo statement) {
        this.name = name;
        this.returnType = TypeName.get(returnType);
        this.classPropertyMap = classPropertyMap;
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

    public StatementInfo getStatement() {
        return statement;
    }

    public boolean useClassParam(){
        return !classPropertyMap.isEmpty();
    }

    public List<SimpleParamInfo> getSimpleParams() {
        return params.stream()
                .filter(SimpleParamInfo.class::isInstance)
                .map(p -> (SimpleParamInfo) p)
                .toList();
    }

    public Map<String, List<ParamInfo>> getClassPropertyMap() {
        return classPropertyMap;
    }

    public boolean unParameterizedStatement() {
        return params.isEmpty() && classPropertyMap.isEmpty();
    }

}
