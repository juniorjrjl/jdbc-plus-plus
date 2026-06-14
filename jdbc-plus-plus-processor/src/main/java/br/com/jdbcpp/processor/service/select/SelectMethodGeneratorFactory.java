package br.com.jdbcpp.processor.service.select;

import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.TypeUtil;

import javax.lang.model.util.Types;

public class SelectMethodGeneratorFactory {

    private final Types types;
    private final SelectMethodGenerator selectCollectionMethodGenerator;
    private final SelectMethodGenerator selectOptionalMethodGenerator;
    private final SelectMethodGenerator selectSingleMethodGenerator;

    public SelectMethodGeneratorFactory(final Types types,
                                        final SelectMethodGenerator selectCollectionMethodGenerator,
                                        final SelectMethodGenerator selectOptionalMethodGenerator,
                                        final SelectMethodGenerator selectSingleMethodGenerator) {
        this.types = types;
        this.selectCollectionMethodGenerator = selectCollectionMethodGenerator;
        this.selectOptionalMethodGenerator = selectOptionalMethodGenerator;
        this.selectSingleMethodGenerator = selectSingleMethodGenerator;
    }

    public SelectMethodGenerator create(final SelectMethodInfo selectMethodInfo) {
        if (CollectionUtil.isCollectionType(selectMethodInfo.getReturnTypeMirror(), types)) {
            return selectCollectionMethodGenerator;
        }

        if (TypeUtil.isOptionalType(selectMethodInfo.getReturnTypeMirror(), types)) {
            return selectOptionalMethodGenerator;
        }
        return selectSingleMethodGenerator;
    }
}
