package br.com.jdbcpp.processor.dto.result;

import br.com.jdbcpp.processor.dto.ParamKind;
import org.jspecify.annotations.Nullable;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public class SetterStrategy extends SelectReturnStrategy<SetterStrategy> {

    private final String methodName;
    @Nullable
    private final String customReturnType;

    public SetterStrategy(final String methodName,
                          @Nullable
                          final String customReturnType,
                          final String name,
                          final TypeMirror type,
                          final ParamKind paramKind,
                          final List<SetterStrategy> nestedValues,
                          @Nullable
                          final TypeMirror genericType,
                          @Nullable
                          final Integer resultSetIndex) {
        super(name, type, paramKind, nestedValues, genericType, resultSetIndex);
        this.methodName = methodName;
        this.customReturnType = customReturnType;
    }

    public String getMethodName() {
        return methodName;
    }

    public @Nullable String getCustomReturnType() {
        return customReturnType;
    }

}
