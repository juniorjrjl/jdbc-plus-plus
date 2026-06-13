package br.com.jdbcpp.processor.service.select;

import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import br.com.jdbcpp.processor.service.select.result.SelectResultSetDelegator;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.TypeUtil;

import javax.lang.model.util.Types;

public class SelectMethodGeneratorFactory {

    private final Types types;
    private final SelectResultSetDelegator selectResultSetDelegator;

    public SelectMethodGeneratorFactory(final Types types, final SelectResultSetDelegator selectResultSetDelegator) {
        this.types = types;
        this.selectResultSetDelegator = selectResultSetDelegator;
    }

    public SelectMethodGenerator create(final SelectMethodInfo selectMethodInfo) {
        if (CollectionUtil.isCollectionType(selectMethodInfo.getReturnTypeMirror(), types)) {
            return new SelectCollectionMethodGenerator(types, selectResultSetDelegator);
        }

        if (TypeUtil.isOptionalType(selectMethodInfo.getReturnTypeMirror(), types)) {
            return new SelectOptionalMethodGenerator(types, selectResultSetDelegator);
        }
        return new SelectSingleMethodGenerator(types, selectResultSetDelegator);
    }
}
