package br.com.jdbcpp.processor.dto.method;

import br.com.jdbcpp.api.method.write.CommandType;
import br.com.jdbcpp.processor.dto.method.customization.OperationCustomize;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract sealed class MethodInfo permits DeleteMethod, InsertMethod, UpdateMethod,
        SelectNullableMethodInfo, SelectOptionalMethodInfo, SelectCollectionMethodInfo {

    protected final String name;
    protected final TypeMirror returnType;
    protected final List<ParamInfo> params;
    protected final Map<String, List<ParamInfo>> classPropertyMap;
    protected final StatementInfo statement;
    protected final TypeMirror packException;
    protected List<? extends AnnotationMirror> annotations;
    @Nullable
    protected OperationCustomize operationCustomize;

    protected MethodInfo(final String name,
                         final TypeMirror returnType,
                         final List<ParamInfo> params,
                         final Map<String, List<ParamInfo>> classPropertyMap,
                         final StatementInfo statement,
                         final TypeMirror packException,
                         final List<? extends AnnotationMirror> annotations,
                         @Nullable
                         final OperationCustomize operationCustomize) {
        this.name = name;
        this.returnType = returnType;
        this.classPropertyMap = classPropertyMap;
        this.params = params;
        this.statement = statement;
        this.packException = packException;
        this.annotations = annotations;
        this.operationCustomize = operationCustomize;
    }

    public static MethodInfoBuilder builder() {
        return new MethodInfoBuilder();
    }

    public String getName() {
        return name;
    }

    public TypeMirror getReturnType() {
        return returnType;
    }

    public List<ParamInfo> getParams() {
        return params;
    }

    public StatementInfo getStatement() {
        return statement;
    }

    public TypeMirror getPackException() {
        return packException;
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

    public List<? extends AnnotationMirror> getAnnotations() {
        return annotations;
    }

    @Nullable
    public OperationCustomize getOperationCustomize() {
        return operationCustomize;
    }

    public static class MethodInfoBuilder {

        @Nullable
        protected String name;
        @Nullable
        protected TypeMirror returnType;
        protected final List<ParamInfo> params = new ArrayList<>();
        protected final Map<String, List<ParamInfo>> classPropertyMap = new HashMap<>();
        @Nullable
        protected StatementInfo statement;
        @Nullable
        protected TypeMirror packException;
        @Nullable
        protected List<? extends AnnotationMirror> annotations;
        @Nullable
        protected OperationCustomize operationCustomize;

        public MethodInfoBuilder withName(final String name) {
            this.name = name;
            return this;
        }

        public MethodInfoBuilder withReturnType(final TypeMirror returnType) {
            this.returnType = returnType;
            return this;
        }

        public MethodInfoBuilder withParams(final List<ParamInfo> params) {
            this.params.addAll(params);
            return this;
        }

        public MethodInfoBuilder withClassPropertyMap(final Map<String, List<ParamInfo>> classPropertyMap) {
            this.classPropertyMap.putAll(classPropertyMap);
            return this;
        }

        public  MethodInfoBuilder withStatement(final StatementInfo statement) {
            this.statement = statement;
            return this;
        }

        public MethodInfoBuilder withPackException(final TypeMirror packException) {
            this.packException = packException;
            return this;
        }

        public MethodInfoBuilder withAnnotations(final List<? extends AnnotationMirror> annotations) {
            this.annotations = annotations;
            return this;
        }

        public MethodInfoBuilder withOperationCustomize(@Nullable final OperationCustomize operationCustomize) {
            this.operationCustomize = operationCustomize;
            return this;
        }

        @SuppressWarnings("unchecked")
        public <T extends MethodInfoBuilder> T asWriteType(final CommandType commandType){
            return (T) switch (commandType){
                case INSERT -> new InsertMethod.InsertMethodBuilder(this);
                case UPDATE -> new UpdateMethod.UpdateMethodBuilder(this);
                case DELETE -> new DeleteMethod.DeleteMethodBuilder(this);
            };
        }

        public <T extends MethodInfoBuilder> T asReadType(final QueryType queryType) {
            return (T) switch (queryType) {
                case NULLABLE -> new SelectNullableMethodInfo.SelectNullableMethodInfoBuilder(this);
                case OPTIONAL -> new SelectOptionalMethodInfo.SelectOptionalMethodInfoBuilder(this);
                case COLLECTION -> new SelectCollectionMethodInfo.SelectCollectionMethodInfoBuilder(this);
            };
        }

    }

}
