package br.com.jdbcpp.processor.service;

import br.com.jdbcpp.processor.dto.DAOImplInfo;
import br.com.jdbcpp.processor.dto.constructor.ConstructorParamInfo;
import br.com.jdbcpp.processor.dto.method.DeleteMethod;
import br.com.jdbcpp.processor.dto.method.InsertMethod;
import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import br.com.jdbcpp.processor.dto.method.UpdateMethod;
import br.com.jdbcpp.processor.service.read.select.SelectCollectionMethodGenerator;
import br.com.jdbcpp.processor.service.read.select.SelectOptionalMethodGenerator;
import br.com.jdbcpp.processor.service.read.select.SelectSingleMethodGenerator;
import br.com.jdbcpp.processor.service.write.delete.DeleteMethodGenerator;
import br.com.jdbcpp.processor.service.write.insert.InsertMethodGenerator;
import br.com.jdbcpp.processor.service.write.update.UpdateMethodGenerator;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.TypeUtil;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

public class DAOGenerator {

    private final SelectCollectionMethodGenerator selectCollectionMethodGenerator;
    private final SelectOptionalMethodGenerator selectOptionalMethodGenerator;
    private final SelectSingleMethodGenerator selectSingleMethodGenerator;
    private final InsertMethodGenerator insertMethodGenerator;
    private final UpdateMethodGenerator updateMethodGenerator;
    private final DeleteMethodGenerator deleteMethodGenerator;
    private final CollectionUtil collectionUtil;
    private final TypeUtil typeUtil;

    public DAOGenerator(final SelectCollectionMethodGenerator selectCollectionMethodGenerator,
                        final SelectOptionalMethodGenerator selectOptionalMethodGenerator,
                        final SelectSingleMethodGenerator selectSingleMethodGenerator,
                        final InsertMethodGenerator insertMethodGenerator,
                        final UpdateMethodGenerator updateMethodGenerator,
                        final DeleteMethodGenerator deleteMethodGenerator,
                        final CollectionUtil collectionUtil,
                        final TypeUtil typeUtil) {
        this.selectCollectionMethodGenerator = selectCollectionMethodGenerator;
        this.selectOptionalMethodGenerator = selectOptionalMethodGenerator;
        this.selectSingleMethodGenerator = selectSingleMethodGenerator;
        this.insertMethodGenerator = insertMethodGenerator;
        this.updateMethodGenerator = updateMethodGenerator;
        this.deleteMethodGenerator = deleteMethodGenerator;
        this.collectionUtil = collectionUtil;
        this.typeUtil = typeUtil;
    }

    public JavaFile build(final DAOImplInfo daoImplInfo) {
        final var daoParent = ClassName.bestGuess(daoImplInfo.name());
        final var implSimpleName = daoParent.simpleName() + "Impl";
        final var daoBuilder = TypeSpec.classBuilder(implSimpleName).addModifiers(PUBLIC);

        final var constructor = daoImplInfo.constructor();
        if (isNull(constructor)) {
            buildImplementInterface(daoBuilder, daoParent, "dataSource");
        } else {
            buildExtendSuperClass(daoBuilder, daoParent, constructor.params());
        }

        final var connectionCall = Optional.ofNullable(constructor)
                .stream()
                .flatMap(c -> c.params().stream())
                .filter(p -> TypeName.get(p.type()).equals(TypeName.get(DataSource.class)))
                .map(ConstructorParamInfo::name)
                .map(v -> v + ".getConnection()")
                .findFirst()
                .orElse("dataSource.getConnection()");

        daoImplInfo.methods().forEach(m -> {
            final var builder = switch (m){
                case InsertMethod insertMethod ->
                        insertMethodGenerator.build(insertMethod, connectionCall);

                case SelectMethodInfo selectMethodInfo
                        when nonNull(selectMethodInfo.getInstanceContainer()) &&
                        collectionUtil.isCollectionType(requireNonNull(selectMethodInfo.getContainerReturnTypeMirror()))
                        -> selectCollectionMethodGenerator.build(selectMethodInfo, connectionCall);

                case SelectMethodInfo selectMethodInfo
                        when nonNull(selectMethodInfo.getInstanceContainer()) &&
                        typeUtil.isOptionalType(requireNonNull(selectMethodInfo.getContainerReturnTypeMirror()))
                        -> selectOptionalMethodGenerator.build(selectMethodInfo, connectionCall);

                case SelectMethodInfo selectMethodInfo ->
                        selectSingleMethodGenerator.build(selectMethodInfo, connectionCall);
                
                case UpdateMethod updateMethod ->
                        updateMethodGenerator.build(updateMethod, connectionCall);
                case DeleteMethod deleteMethod ->
                        deleteMethodGenerator.build(deleteMethod, connectionCall);
            };
            daoBuilder.addMethod(builder.build());
        });
        return JavaFile.builder(daoImplInfo.packageName(), daoBuilder.build()).build();
    }

    private void buildImplementInterface(final TypeSpec.Builder classBuilder,
                                         final ClassName interfaceName,
                                         final String dataSourceVar) {
        classBuilder.addSuperinterface(interfaceName)
                .addField(DataSource.class, dataSourceVar, PRIVATE, FINAL);
        final var constructor = MethodSpec.constructorBuilder()
                .addModifiers(PUBLIC)
                .addParameter(DataSource.class, dataSourceVar, FINAL)
                .addStatement("this.$L = $L", dataSourceVar, dataSourceVar)
                .build();
        classBuilder.addMethod(constructor);
    }

    private void buildExtendSuperClass(final TypeSpec.Builder classBuilder,
                                       final ClassName abstractClass,
                                       final List<ConstructorParamInfo> constructorParams){
        final var ctorBuilder = MethodSpec.constructorBuilder().addModifiers(PUBLIC);
        constructorParams.forEach(
                p -> ctorBuilder.addParameter(TypeName.get(p.type()), p.name(), FINAL)
        );
        final var joinedParams = constructorParams.stream()
                .map(ConstructorParamInfo::name)
                .collect(Collectors.joining(", "));
        final var constructor = ctorBuilder.addStatement("super($L)", joinedParams)
                .build();
        classBuilder.superclass(abstractClass);
        classBuilder.addMethod(constructor);
    }

}
