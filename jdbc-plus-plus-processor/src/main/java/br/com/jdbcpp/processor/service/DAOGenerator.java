package br.com.jdbcpp.processor.service;

import br.com.jdbcpp.processor.dto.DAOImplInfo;
import br.com.jdbcpp.processor.dto.constructor.ConstructorParamInfo;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.exception.InvalidMethodInformationException;
import br.com.jdbcpp.processor.service.dao.MethodGenerator;
import br.com.jdbcpp.processor.util.LambdaUtil;
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
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

public class DAOGenerator {

    private static final String DATA_SOURCE = "dataSource";

    private final List<MethodGenerator<?>> methodGenerators;

    public DAOGenerator(final List<MethodGenerator<?>> methodGenerators) {
        this.methodGenerators = methodGenerators;
    }

    public JavaFile build(final DAOImplInfo daoImplInfo) {
        final var daoParent = ClassName.bestGuess(daoImplInfo.name());
        final var implSimpleName = daoParent.simpleName() + "Impl";
        final var daoBuilder = TypeSpec.classBuilder(implSimpleName).addModifiers(PUBLIC);

        final var constructor = daoImplInfo.constructor();
        if (isNull(constructor)) {
            buildImplementInterface(daoBuilder, daoParent);
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
                .orElse(DATA_SOURCE + ".getConnection()");


        daoImplInfo.methods().forEach(LambdaUtil.unchecked(m ->{
            @SuppressWarnings("unchecked")
            final var methodSpec = methodGenerators.stream().filter(g -> g.useInstance(m))
                    .findFirst()
                    .map(g -> ((MethodGenerator<MethodInfo>) g).build(m, connectionCall))
                    .map(MethodSpec.Builder::build)
                    .orElseThrow(LambdaUtil.unchecked(() -> new InvalidMethodInformationException(
                            String.format("A method %s contains a unknow error", m.getName())
                    )));
            daoBuilder.addMethod(methodSpec);
        }));

        return JavaFile.builder(daoImplInfo.packageName(), daoBuilder.build()).build();
    }

    private void buildImplementInterface(final TypeSpec.Builder classBuilder,
                                         final ClassName interfaceName) {
        classBuilder.addSuperinterface(interfaceName)
                .addField(DataSource.class, DATA_SOURCE, PRIVATE, FINAL);
        final var constructor = MethodSpec.constructorBuilder()
                .addModifiers(PUBLIC)
                .addParameter(DataSource.class, DATA_SOURCE, FINAL)
                .addStatement("this.$L = $L", DATA_SOURCE, DATA_SOURCE)
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
