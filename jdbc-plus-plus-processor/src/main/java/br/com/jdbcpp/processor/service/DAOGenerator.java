package br.com.jdbcpp.processor.service;

import br.com.jdbcpp.processor.dto.DAOImplInfo;
import br.com.jdbcpp.processor.dto.method.DeleteMethod;
import br.com.jdbcpp.processor.dto.method.InsertMethod;
import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import br.com.jdbcpp.processor.dto.method.UpdateMethod;
import br.com.jdbcpp.processor.service.write.delete.DeleteMethodGenerator;
import br.com.jdbcpp.processor.service.write.insert.InsertMethodGenerator;
import br.com.jdbcpp.processor.service.read.select.SelectCollectionMethodGenerator;
import br.com.jdbcpp.processor.service.read.select.SelectOptionalMethodGenerator;
import br.com.jdbcpp.processor.service.read.select.SelectSingleMethodGenerator;
import br.com.jdbcpp.processor.service.write.update.UpdateMethodGenerator;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.TypeUtil;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;

import javax.lang.model.util.Types;

import static javax.lang.model.element.Modifier.PUBLIC;

public class DAOGenerator {

    private final Types types;
    private final SelectCollectionMethodGenerator selectCollectionMethodGenerator;
    private final SelectOptionalMethodGenerator selectOptionalMethodGenerator;
    private final SelectSingleMethodGenerator selectSingleMethodGenerator;
    private final InsertMethodGenerator insertMethodGenerator;
    private final UpdateMethodGenerator updateMethodGenerator;
    private final DeleteMethodGenerator deleteMethodGenerator;

    public DAOGenerator(final Types types,
                        final SelectCollectionMethodGenerator selectCollectionMethodGenerator,
                        final SelectOptionalMethodGenerator selectOptionalMethodGenerator,
                        final SelectSingleMethodGenerator selectSingleMethodGenerator,
                        final InsertMethodGenerator insertMethodGenerator,
                        final UpdateMethodGenerator updateMethodGenerator,
                        final DeleteMethodGenerator deleteMethodGenerator) {
        this.types = types;
        this.selectCollectionMethodGenerator = selectCollectionMethodGenerator;
        this.selectOptionalMethodGenerator = selectOptionalMethodGenerator;
        this.selectSingleMethodGenerator = selectSingleMethodGenerator;
        this.insertMethodGenerator = insertMethodGenerator;
        this.updateMethodGenerator = updateMethodGenerator;
        this.deleteMethodGenerator = deleteMethodGenerator;
    }

    public JavaFile build(final DAOImplInfo daoImplInfo) {
        final var daoInterface = ClassName.get(daoImplInfo.packageName(), daoImplInfo.name());
        final var implSimpleName = daoImplInfo.name() + "Impl";
        final var daoImpl = ClassName.get(daoImplInfo.packageName(), implSimpleName);

        final var daoBuilder = TypeSpec.classBuilder(implSimpleName)
                .addModifiers(PUBLIC)
                .addSuperinterface(daoInterface);

        daoImplInfo.methods().forEach(m -> {
            final var connectionCall = "connection.getConnection()";
            final var builder = switch (m){
                case InsertMethod insertMethod ->
                        insertMethodGenerator.build(insertMethod, connectionCall);
                case SelectMethodInfo selectMethodInfo ->{
                    if (CollectionUtil.isCollectionType(selectMethodInfo.getReturnTypeMirror(), types)) {
                        yield selectCollectionMethodGenerator.build(selectMethodInfo, connectionCall);
                    }

                    if (TypeUtil.isOptionalType(selectMethodInfo.getReturnTypeMirror(), types)) {
                        yield selectOptionalMethodGenerator.build(selectMethodInfo, connectionCall);
                    }
                    yield selectSingleMethodGenerator.build(selectMethodInfo, connectionCall);
                }
                case UpdateMethod updateMethod ->
                        updateMethodGenerator.build(updateMethod, connectionCall);
                case DeleteMethod deleteMethod ->
                        deleteMethodGenerator.build(deleteMethod, connectionCall);
            };
            daoBuilder.addMethod(builder.build());
        });

        return JavaFile.builder(daoImplInfo.packageName(), daoBuilder.build()).build();
    }

}
