package br.com.jdbcpp.processor.dto.result;

import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import java.util.Collections;

import static br.com.jdbcpp.processor.dto.ParamKind.COLLECTION_JAVA_TYPE;
import static br.com.jdbcpp.processor.dto.ParamKind.JAVA_TYPE;
import static java.util.Objects.isNull;

public class SimpleResultStrategy extends SelectReturnStrategy<SimpleResultStrategy>{

    public SimpleResultStrategy(final TypeName type,
                                final @Nullable TypeName genericType) {
        super("unnamed",
                type,
                isNull(genericType) ?
                        JAVA_TYPE :
                        COLLECTION_JAVA_TYPE,
                Collections.emptyList(),
                genericType,
                -1
        );
    }

}
