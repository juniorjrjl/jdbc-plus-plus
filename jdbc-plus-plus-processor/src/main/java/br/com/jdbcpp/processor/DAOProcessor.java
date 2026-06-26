package br.com.jdbcpp.processor;

import br.com.jdbcpp.api.Command;
import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.api.Query;
import br.com.jdbcpp.processor.dto.DAOImplInfo;
import br.com.jdbcpp.processor.dto.constructor.ConstructorInfo;
import br.com.jdbcpp.processor.dto.constructor.ConstructorParamInfo;
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
import br.com.jdbcpp.processor.util.ArrayUtil;
import br.com.jdbcpp.processor.util.CollectionUtil;
import com.google.auto.service.AutoService;
import com.palantir.javapoet.ArrayTypeName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
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
import static javax.lang.model.SourceVersion.RELEASE_21;

@SupportedAnnotationTypes("br.com.jdbcpp.api.DAO")
@AutoService(Processor.class)
@SupportedSourceVersion(RELEASE_21)
public class DAOProcessor extends AbstractProcessor {

    private static final String DATA_SOURCE_CANONICAL_NAME = DataSource.class.getCanonicalName();

    @Nullable
    private DAOGenerator daoGeneratorCache;
    @Nullable
    private ParameterInfoDelegator parameterInfoDelegatorCache;

    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
                           final RoundEnvironment roundEnv) {
        final var types = processingEnv.getTypeUtils();
        final var elements = processingEnv.getElementUtils();
        final var messager = processingEnv.getMessager();
        final var filer = processingEnv.getFiler();
        final var dataSourceElement = Optional.ofNullable(elements.getTypeElement(DATA_SOURCE_CANONICAL_NAME))
                .map(TypeElement::asType)
                .orElseThrow();
        final var mappedDAOs = roundEnv.getElementsAnnotatedWith(DAO.class)
                .stream()
                .toList();

        if (mappedDAOs.isEmpty()) {
            return true;
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

            try {
                final var constructor = isValidDAO(mappedDAO, elements, types, dataSourceElement);
                final var constructorInfo = buildConstructorInfo(constructor, types);
                daoImplInfoBuilder.constructor(constructorInfo);
            } catch (final InvalidDAOException e){
                messager.printError(e.getMessage(), e.getElement());
            }

            if (methods.isEmpty()) {
                final var message = String.format(
                        "DAO interface %s must have at least one method annotated with @Query or @Commnad",
                        className
                );
                messager.printError(message, mappedDAO);
            }

            for(final var method: methods){
                try {
                    final var methodInfo = buildMethodInfo(method, types);
                    methodsInfo.add(methodInfo);
                } catch (final MoreParamsThanStatementNeedException e){
                    messager.printWarning(e.getMessage(), method);
                } catch (final JDBCPlusPlusProcessorException e) {
                    messager.printError(e.getMessage(), method);
                }
            }

            final var daoGenerator = buildDAOGenerator(types);
            daoImplInfos.add(daoImplInfoBuilder.methods(methodsInfo).build());
            final var javaFiles = daoImplInfos.stream().map(daoGenerator::build).toList();
            javaFiles.forEach(f -> writeClass(f, messager, filer));
        }

        return true;
    }

    @Nullable
    private ConstructorInfo buildConstructorInfo(@Nullable
                                                 final ExecutableElement constructor,
                                                 final Types types){
        if (isNull(constructor)) {
            return null;
        }
        final var params = constructor.getParameters()
                .stream()
                .map(p -> {
                    TypeName type = TypeName.get(p.asType());
                    if (ArrayUtil.isArray(p.asType())){
                        type = ArrayTypeName.of(TypeName.get(p.asType()));
                    } else if (CollectionUtil.isCollectionType(p.asType(), types)){
                        type = TypeName.get(types.erasure(p.asType()));
                    }
                    return new ConstructorParamInfo(
                            p.getSimpleName().toString(),
                            type
                    );
                })
                .toList();
        return new ConstructorInfo(params);
    }

    @Nullable
    private ExecutableElement isValidDAO(final Element mappedDAO,
                                         final Elements elements,
                                         final Types types,
                                         final TypeMirror dataSourceElement) {
        final var className = elements.getTypeElement(mappedDAO.toString()).toString();
        if (mappedDAO.getKind() == ElementKind.INTERFACE){
            return null;
        }

        if (mappedDAO.getKind() == ElementKind.CLASS){
            if (!mappedDAO.getModifiers().contains(Modifier.ABSTRACT)) {
                final var message = String.format(
                        "Invalid DAO %s: A DAO annotation is used on a abstract classes or interfaces",
                        className
                );
                throw new InvalidDAOException(message, mappedDAO);
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
                        return new InvalidDAOException(message, mappedDAO);
                    });
            return mappedDAO.getEnclosedElements().stream()
                    .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
                    .map(ExecutableElement.class::cast)
                    .filter(e -> e.getModifiers().contains(Modifier.PUBLIC) || e.getModifiers().contains(Modifier.PROTECTED))
                    .filter(c -> c.getParameters().stream()
                            .anyMatch(p -> p.asType().equals(dataSourceElement)))
                    .findFirst()
                    .orElseThrow(() -> {
                        final var message = String.format(
                                "Invalid DAO %s: For DAO abstract classes is required exactly one constructor with a param type %s",
                                DATA_SOURCE_CANONICAL_NAME,
                                className
                        );
                        return new InvalidDAOException(message, mappedDAO);
                    });
        } else {
            return null;
        }
    }

    private MethodInfo buildMethodInfo(final ExecutableElement method,
                                       final Types types) throws JDBCPlusPlusProcessorException {
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

    private DAOGenerator buildDAOGenerator(final Types types) {
        if (isNull(daoGeneratorCache)) {
            final var statementBuilder = new StatementBuilder();
            final var selectResultSetDelegator = new SelectResultSetDelegator(
                    new SelectResultUsingConstructor(),
                    new SelectResultUsingSetter(),
                    new SelectResultSimpleResult()
            );
            this.daoGeneratorCache = new DAOGenerator(
                    types,
                    new SelectCollectionMethodGenerator(types, selectResultSetDelegator, statementBuilder),
                    new SelectOptionalMethodGenerator(types, selectResultSetDelegator, statementBuilder),
                    new SelectSingleMethodGenerator(types, selectResultSetDelegator, statementBuilder),
                    new InsertMethodGenerator(statementBuilder),
                    new UpdateMethodGenerator(statementBuilder),
                    new DeleteMethodGenerator(statementBuilder)
            );
        }
        return daoGeneratorCache;
    }

    private void writeClass(final JavaFile javaFile, final Messager messager, final Filer filer){
        try {
            javaFile.writeTo(filer);
        }catch (IOException ex){
            messager.printError(ex.getMessage());
        }
    }

}
