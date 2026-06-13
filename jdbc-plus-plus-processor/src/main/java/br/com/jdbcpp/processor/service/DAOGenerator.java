package br.com.jdbcpp.processor.service;

import br.com.jdbcpp.processor.dto.DAOImplInfo;
import br.com.jdbcpp.processor.dto.method.DeleteMethod;
import br.com.jdbcpp.processor.dto.method.InsertMethod;
import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import br.com.jdbcpp.processor.dto.method.UpdateMethod;
import br.com.jdbcpp.processor.service.delete.DeleteMethodGenerator;
import br.com.jdbcpp.processor.service.insert.InsertMethodGenerator;
import br.com.jdbcpp.processor.service.select.SelectMethodGeneratorFactory;
import br.com.jdbcpp.processor.service.update.UpdateMethodGenerator;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;

import static javax.lang.model.element.Modifier.PUBLIC;

public class DAOGenerator {

    private final SelectMethodGeneratorFactory selectFactory;
    private final InsertMethodGenerator insertMethodGenerator;
    private final UpdateMethodGenerator updateMethodGenerator;
    private final DeleteMethodGenerator deleteMethodGenerator;

    public DAOGenerator(final SelectMethodGeneratorFactory selectFactory,
                        final InsertMethodGenerator insertMethodGenerator,
                        final UpdateMethodGenerator updateMethodGenerator,
                        final DeleteMethodGenerator deleteMethodGenerator) {
        this.selectFactory = selectFactory;
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
            final var method = switch (m){
                case InsertMethod insertMethod ->
                        insertMethodGenerator.build(insertMethod);
                case SelectMethodInfo selectMethodInfo ->
                        selectFactory.create(selectMethodInfo).build(selectMethodInfo);
                case UpdateMethod updateMethod ->
                        updateMethodGenerator.build(updateMethod);
                case DeleteMethod deleteMethod ->
                        deleteMethodGenerator.build(deleteMethod);
            };
            daoBuilder.addMethod(method);
        });

        return JavaFile.builder(daoImplInfo.packageName(), daoBuilder.build()).build();
    }

}
