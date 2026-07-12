package br.com.jdbcpp.processor.dto.result;

import br.com.jdbcpp.processor.dto.ParamKind;
import org.jspecify.annotations.Nullable;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public class SetterStrategy extends SelectReturnStrategy<SetterStrategy> {

    private final String methodName;

    public SetterStrategy(final String methodName,
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
    }


    public String getMethodName() {
        return methodName;
    }
}
