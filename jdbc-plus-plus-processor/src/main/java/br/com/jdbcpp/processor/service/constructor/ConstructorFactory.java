package br.com.jdbcpp.processor.service.constructor;

import br.com.jdbcpp.processor.dto.constructor.ConstructorInfo;
import br.com.jdbcpp.processor.dto.constructor.ConstructorParamInfo;
import br.com.jdbcpp.processor.util.ArrayUtil;
import br.com.jdbcpp.processor.util.CollectionUtil;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import static java.util.Objects.isNull;

public class ConstructorFactory {

    private final ArrayUtil arrayUtil;
    private final CollectionUtil collectionUtil;
    private final Types types;

    public ConstructorFactory(final ArrayUtil arrayUtil,
                              final CollectionUtil collectionUtil,
                              final Types types) {
        this.arrayUtil = arrayUtil;
        this.collectionUtil = collectionUtil;
        this.types = types;
    }


    @Nullable
    public ConstructorInfo build(final @Nullable ExecutableElement constructor){
        if (isNull(constructor)) {
            return null;
        }
        final var params = constructor.getParameters()
                .stream()
                .map(p -> {
                    TypeMirror type = p.asType();
                    if (arrayUtil.isArray(p.asType())){
                        type = p.asType();
                    } else if (collectionUtil.isCollectionType(p.asType())){
                        type = types.erasure(p.asType());
                    }
                    return new ConstructorParamInfo(
                            p.getSimpleName().toString(),
                            type
                    );
                })
                .toList();
        return new ConstructorInfo(params);
    }

}
