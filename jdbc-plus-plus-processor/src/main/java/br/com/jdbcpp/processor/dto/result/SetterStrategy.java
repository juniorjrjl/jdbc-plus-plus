package br.com.jdbcpp.processor.dto.result;

import br.com.jdbcpp.processor.dto.ParamKind;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class SetterStrategy extends SelectReturnStrategy<SetterStrategy> {

    private final String methodName;

    public SetterStrategy(final String methodName,
                          final String name,
                          final TypeName type,
                          final ParamKind paramKind,
                          final List<SetterStrategy> nestedValues,
                          @Nullable
                          final TypeName genericType,
                          @Nullable
                          final Integer resultSetIndex) {
        super(name, type, paramKind, nestedValues, genericType, resultSetIndex);
        this.methodName = methodName;
    }


    public String getMethodName() {
        return methodName;
    }
}
