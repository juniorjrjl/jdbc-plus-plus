package br.com.jdbcpp.processor;

import br.com.jdbcpp.api.Command;
import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.api.Query;
import br.com.jdbcpp.processor.dto.DAOImplInfo;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.method.ReadMethodInfoFactory;
import br.com.jdbcpp.processor.dto.method.WriteMethodInfoFactory;
import br.com.jdbcpp.processor.dto.parameter.ClassParamInfoFactory;
import br.com.jdbcpp.processor.dto.parameter.ParameterInfoDelegator;
import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfoFactory;
import br.com.jdbcpp.processor.exception.InvalidMethodSignature;
import br.com.jdbcpp.processor.exception.JDBCPlusPlusProcessorException;
import br.com.jdbcpp.processor.service.DAOGenerator;
import br.com.jdbcpp.processor.service.delete.DeleteMethodGenerator;
import br.com.jdbcpp.processor.service.insert.InsertMethodGenerator;
import br.com.jdbcpp.processor.service.select.SelectCollectionMethodGenerator;
import br.com.jdbcpp.processor.service.select.SelectMethodGeneratorFactory;
import br.com.jdbcpp.processor.service.select.SelectOptionalMethodGenerator;
import br.com.jdbcpp.processor.service.select.SelectSingleMethodGenerator;
import br.com.jdbcpp.processor.service.select.result.SelectResultSetDelegator;
import br.com.jdbcpp.processor.service.select.result.SelectResultSimpleResult;
import br.com.jdbcpp.processor.service.select.result.SelectResultUsingConstructor;
import br.com.jdbcpp.processor.service.select.result.SelectResultUsingSetter;
import br.com.jdbcpp.processor.service.update.UpdateMethodGenerator;
import com.google.auto.service.AutoService;
import com.palantir.javapoet.JavaFile;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.WARNING;

@SupportedAnnotationTypes("br.com.jdbcpp.api.DAOMethod")
@AutoService(Processor.class)
public class DAOProcessor extends AbstractProcessor {

    private final Types types;
    private final Messager messager;
    private final Filer filer;
    @Nullable
    private DAOGenerator daoGeneratorCache;
    @Nullable
    private ParameterInfoDelegator parameterInfoDelegatorCache;

    public DAOProcessor(){
        super();
        this.types = processingEnv.getTypeUtils();
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
                           final RoundEnvironment roundEnv) {
        final var daoInterfaces = roundEnv.getElementsAnnotatedWith(DAO.class)
                .stream()
                .toList();

        if (daoInterfaces.isEmpty()) {
            messager.printMessage(WARNING, "None DAOs found to generate");
            return true;
        }

        final var elementUtils = processingEnv.getElementUtils();

        final List<DAOImplInfo> daoImplInfos = new ArrayList<>();
        final List<MethodInfo> methodsInfo = new ArrayList<>();
        for (final var daoInterface : daoInterfaces) {
            final var packageName = elementUtils.getPackageOf(daoInterface).toString();
            final var className = elementUtils.getTypeElement(daoInterface.toString()).toString();
            final var daoImplInfoBuilder = DAOImplInfo.builder().name(className).packageName(packageName);
            final var methods = ElementFilter.methodsIn(daoInterface.getEnclosedElements()).stream()
                    .filter(m -> nonNull(m.getAnnotation(Query.class)) || nonNull(m.getAnnotation(Command.class)))
                    .toList();

            if (methods.isEmpty()) {
                final var message = String.format(
                        "DAO interface %s must have at least one method annotated with @Query or @Commnad",
                        className
                );
                messager.printMessage(ERROR, message);
            }

            for(final var method: methods){
                try{
                    final var methodInfo = buildMethodInfo(method);
                    methodsInfo.add(methodInfo);
                } catch (final JDBCPlusPlusProcessorException e) {
                    messager.printMessage(ERROR, e.getMessage());
                }
            }

            final var daoGenerator = buildDAOGenerator();
            daoImplInfos.add(daoImplInfoBuilder.methods(methodsInfo).build());
            final var javaFiles = daoImplInfos.stream().map(daoGenerator::build).toList();
            javaFiles.forEach(this::writeClass);
        }

        return true;
    }

    private MethodInfo buildMethodInfo(final ExecutableElement method) throws JDBCPlusPlusProcessorException {
        final var parameterInfoDelegator = buildParameterInfoDelegator();

        final var params = parameterInfoDelegator.create(
                method.getSimpleName().toString(),
                method.getParameters(),
                types
        );

        final var commandOptional = Optional.ofNullable(method.getAnnotation(Command.class))
                .map(command -> WriteMethodInfoFactory.create(method, params, command));

        return Optional.ofNullable(method.getAnnotation(Query.class))
                .map(query -> ReadMethodInfoFactory.create(method, params, query, types))
                .or(() -> commandOptional)
                .orElseThrow(() -> {
                    final var message = String.format("Fail to get info from method %s", method.getSimpleName());
                    return new InvalidMethodSignature(message);
                });
    }

    private ParameterInfoDelegator buildParameterInfoDelegator() {
        if (isNull(parameterInfoDelegatorCache)) {
            parameterInfoDelegatorCache =  new ParameterInfoDelegator(
                    new SimpleParamInfoFactory(),
                    new ClassParamInfoFactory()
            );
        }
        return this.parameterInfoDelegatorCache;
    }

    private DAOGenerator buildDAOGenerator() {
        if (isNull(daoGeneratorCache)) {
            final var selectResultSetDelegator = new SelectResultSetDelegator(
                    new SelectResultUsingConstructor(),
                    new SelectResultUsingSetter(),
                    new SelectResultSimpleResult()
            );
            final var selectMethodGeneratorFactory = new SelectMethodGeneratorFactory(
                    this.types,
                    new SelectCollectionMethodGenerator(this.types, selectResultSetDelegator),
                    new SelectOptionalMethodGenerator(this.types, selectResultSetDelegator),
                    new SelectSingleMethodGenerator(this.types, selectResultSetDelegator)
            );
            this.daoGeneratorCache = new DAOGenerator(
                    selectMethodGeneratorFactory,
                    new InsertMethodGenerator(),
                    new UpdateMethodGenerator(),
                    new DeleteMethodGenerator()
            );
        }
        return daoGeneratorCache;
    }

    private void writeClass(final JavaFile javaFile){
        try {
            javaFile.writeTo(filer);
        }catch (IOException ex){
            messager.printMessage(ERROR, ex.getMessage());
        }
    }

}
