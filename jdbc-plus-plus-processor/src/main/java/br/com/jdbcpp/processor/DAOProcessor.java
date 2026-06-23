package br.com.jdbcpp.processor;

import br.com.jdbcpp.api.Command;
import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.api.Query;
import br.com.jdbcpp.processor.dto.DAOImplInfo;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.method.ReadMethodInfoFactory;
import br.com.jdbcpp.processor.dto.method.WriteMethodInfoFactory;
import br.com.jdbcpp.processor.dto.parameter.ClassParamInfo;
import br.com.jdbcpp.processor.dto.parameter.ClassParamInfoFactory;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.parameter.ParamPathExtractor;
import br.com.jdbcpp.processor.dto.parameter.ParameterInfoDelegator;
import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfoFactory;
import br.com.jdbcpp.processor.exception.InvalidDAOException;
import br.com.jdbcpp.processor.exception.InvalidMethodSignatureException;
import br.com.jdbcpp.processor.exception.JDBCPlusPlusProcessorException;
import br.com.jdbcpp.processor.exception.MoreParamsThanStatementNeedException;
import br.com.jdbcpp.processor.service.DAOGenerator;
import br.com.jdbcpp.processor.service.read.select.SelectCollectionMethodGenerator;
import br.com.jdbcpp.processor.service.read.select.SelectOptionalMethodGenerator;
import br.com.jdbcpp.processor.service.read.select.SelectSingleMethodGenerator;
import br.com.jdbcpp.processor.service.read.select.result.SelectResultSetDelegator;
import br.com.jdbcpp.processor.service.read.select.result.SelectResultSimpleResult;
import br.com.jdbcpp.processor.service.read.select.result.SelectResultUsingConstructor;
import br.com.jdbcpp.processor.service.read.select.result.SelectResultUsingSetter;
import br.com.jdbcpp.processor.service.statement.StatementBuilder;
import br.com.jdbcpp.processor.service.write.delete.DeleteMethodGenerator;
import br.com.jdbcpp.processor.service.write.insert.InsertMethodGenerator;
import br.com.jdbcpp.processor.service.write.update.UpdateMethodGenerator;
import com.google.auto.service.AutoService;
import com.palantir.javapoet.JavaFile;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private final Elements elements;
    private final Messager messager;
    private final Filer filer;
    @Nullable
    private DAOGenerator daoGeneratorCache;
    @Nullable
    private ParameterInfoDelegator parameterInfoDelegatorCache;

    public DAOProcessor(){
        super();
        this.types = processingEnv.getTypeUtils();
        this.elements = processingEnv.getElementUtils();
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
                           final RoundEnvironment roundEnv) {
        final var mappedDAOs = roundEnv.getElementsAnnotatedWith(DAO.class)
                .stream()
                .toList();

        if (mappedDAOs.isEmpty()) {
            messager.printMessage(WARNING, "None DAOs found to generate");
            return true;
        }

        try {
            isValidDAO(mappedDAOs, elements, types);
        }catch (final JDBCPlusPlusProcessorException e){
            messager.printMessage(ERROR, e.getMessage());
        }

        final var elementUtils = processingEnv.getElementUtils();

        final List<DAOImplInfo> daoImplInfos = new ArrayList<>();
        final List<MethodInfo> methodsInfo = new ArrayList<>();
        for (final var mappedDAO : mappedDAOs) {
            final var packageName = elementUtils.getPackageOf(mappedDAO).toString();
            final var className = elementUtils.getTypeElement(mappedDAO.toString()).toString();
            final var daoImplInfoBuilder = DAOImplInfo.builder().name(className).packageName(packageName);
            final var methods = ElementFilter.methodsIn(mappedDAO.getEnclosedElements()).stream()
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
                try {
                    final var methodInfo = buildMethodInfo(method);
                    methodsInfo.add(methodInfo);
                } catch (final MoreParamsThanStatementNeedException e){
                    messager.printMessage(WARNING, e.getMessage());
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

    private void isValidDAO(final List<? extends Element> mappedDAOs,
                            final Elements elements,
                            final Types types) {
        final var dataSourceCanonicalName = DataSource.class.getCanonicalName();
        final var dataSourceElement = Optional.ofNullable(elements.getTypeElement(dataSourceCanonicalName))
                .map(TypeElement::asType)
                .orElseThrow();
        for(final var mappedDAO: mappedDAOs){
            final var className = elements.getTypeElement(mappedDAO.toString()).toString();
            if (mappedDAO.getKind() == ElementKind.INTERFACE){
                continue;
            }
            if (mappedDAO.getKind() == ElementKind.CLASS){
                if (!mappedDAO.getModifiers().contains(Modifier.ABSTRACT)) {
                    final var message = String.format(
                            "Invalid DAO %s: A DAO annotation is used on a abstract classes or interfaces",
                            className
                    );
                    throw new InvalidDAOException(message);
                }
                mappedDAO.getEnclosedElements().stream()
                        .filter(e -> e.getModifiers().containsAll(List.of(Modifier.PROTECTED, Modifier.FINAL)))
                        .filter(e -> types.isSameType(e.asType(), dataSourceElement))
                        .findFirst()
                        .orElseThrow(() -> {
                            final var message = String.format(
                                    "Invalid DAO %s: For DAO abstract classes a protected final field of type DataSource is required",
                                    className
                            );
                            return new InvalidDAOException(message);
                        });
                if (mappedDAO.getEnclosedElements().stream()
                        .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
                        .map(ExecutableElement.class::cast)
                        .filter(e -> e.getModifiers().contains(Modifier.PUBLIC) || e.getModifiers().contains(Modifier.PROTECTED))
                        .filter(c -> c.getParameters().stream()
                                .anyMatch(p -> p.asType().equals(dataSourceElement)))
                        .count() != 1){
                    final var message = String.format(
                            "Invalid DAO %s: For DAO abstract classes is required exactly one constructor with a param type %s",
                            dataSourceCanonicalName,
                            className
                            );
                    throw new InvalidDAOException(message);
                }

            }
        }
    }

    private MethodInfo buildMethodInfo(final ExecutableElement method) throws JDBCPlusPlusProcessorException {
        final var parameterInfoDelegator = buildParameterInfoDelegator();

        final var params = parameterInfoDelegator.create(
                method.getSimpleName().toString(),
                method.getParameters(),
                types
        );

        final Map<String, List<ParamInfo>> classPropertyMap =
                (params.size() == 1 && params.getFirst() instanceof ClassParamInfo classParamInfo) ?
                ParamPathExtractor.build(classParamInfo) :
                Collections.emptyMap();

        final var commandOptional = Optional.ofNullable(method.getAnnotation(Command.class))
                .map(command -> WriteMethodInfoFactory.create(method, params, classPropertyMap, command));

        return Optional.ofNullable(method.getAnnotation(Query.class))
                .map(query -> ReadMethodInfoFactory.create(method, params, classPropertyMap, query, types))
                .or(() -> commandOptional)
                .orElseThrow(() -> {
                    final var message = String.format("Fail to get info from method %s", method.getSimpleName());
                    return new InvalidMethodSignatureException(message);
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
            final var statementBuilder = new StatementBuilder();
            final var selectResultSetDelegator = new SelectResultSetDelegator(
                    new SelectResultUsingConstructor(),
                    new SelectResultUsingSetter(),
                    new SelectResultSimpleResult()
            );
            this.daoGeneratorCache = new DAOGenerator(
                    this.types,
                    new SelectCollectionMethodGenerator(this.types, selectResultSetDelegator, statementBuilder),
                    new SelectOptionalMethodGenerator(this.types, selectResultSetDelegator, statementBuilder),
                    new SelectSingleMethodGenerator(this.types, selectResultSetDelegator, statementBuilder),
                    new InsertMethodGenerator(statementBuilder),
                    new UpdateMethodGenerator(statementBuilder),
                    new DeleteMethodGenerator(statementBuilder)
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
