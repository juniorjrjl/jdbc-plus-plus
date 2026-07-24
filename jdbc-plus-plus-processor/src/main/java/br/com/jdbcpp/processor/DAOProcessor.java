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
import br.com.jdbcpp.processor.service.read.select.result.SelectResultSimpleResultList;
import br.com.jdbcpp.processor.service.read.select.result.SelectResultUsingConstructor;
import br.com.jdbcpp.processor.service.read.select.result.SelectResultUsingSetter;
import br.com.jdbcpp.processor.service.statement.StatementBuilder;
import br.com.jdbcpp.processor.service.write.delete.DeleteMethodGenerator;
import br.com.jdbcpp.processor.service.write.insert.InsertMethodGenerator;
import br.com.jdbcpp.processor.service.write.update.UpdateMethodGenerator;
import br.com.jdbcpp.processor.util.ArrayUtil;
import br.com.jdbcpp.processor.util.BuildConstructorStrategy;
import br.com.jdbcpp.processor.util.BuildSetterStrategy;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.MethodValidator;
import br.com.jdbcpp.processor.util.TypeUtil;
import com.google.auto.service.AutoService;
import com.palantir.javapoet.JavaFile;
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
import javax.lang.model.type.TypeKind;
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

        for (final var mappedDAO : mappedDAOs) {
            final var packageName = elementUtils.getPackageOf(mappedDAO).toString();
            final var className = elementUtils.getTypeElement(mappedDAO.toString()).toString();
            final var daoImplInfoBuilder = DAOImplInfo.builder().name(className).packageName(packageName);
            final var methods = ElementFilter.methodsIn(mappedDAO.getEnclosedElements()).stream()
                    .filter(m -> nonNull(m.getAnnotation(Query.class)) || nonNull(m.getAnnotation(Command.class)))
                    .toList();

            try {
                final var constructor = isValidDAO(mappedDAO, elements, types, dataSourceElement);
                final var constructorInfo = buildConstructorInfo(constructor, types, elements);
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

            final List<MethodInfo> methodsInfo = new ArrayList<>();
            for(final var method: methods){
                try {
                    final var methodInfo = buildMethodInfo(method, elements, types);
                    methodsInfo.add(methodInfo);
                } catch (final MoreParamsThanStatementNeedException e){
                    messager.printWarning(e.getMessage(), method);
                } catch (final JDBCPlusPlusProcessorException e) {
                    messager.printError(e.getMessage(), method);
                }
            }

            final var daoGenerator = buildDAOGenerator(types);
            final var daoImplInfo = daoImplInfoBuilder.methods(methodsInfo).build();
            final var javaFile = daoGenerator.build(daoImplInfo);
            writeClass(javaFile, messager, filer);
        }

        return true;
    }

    @Nullable
    private ConstructorInfo buildConstructorInfo(@Nullable
                                                 final ExecutableElement constructor,
                                                 final Types types,
                                                 final Elements elements){
        if (isNull(constructor)) {
            return null;
        }
        final var arrayUtil = buildArrayUtil(types, elements);
        final var collectionUtil = buildCollectionUtil(types);
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
                                       final Elements elements,
                                       final Types types) throws JDBCPlusPlusProcessorException {
        final var parameterInfoDelegator = buildParameterInfoDelegator(types, elements);
        final var methodValidator = buildMethodValidator(elements, types);
        final var readMethodInfoFactory = buildReadMethodInfoFactory(types, elements);
        final var typeUtil = buildTypeUtil(types, elements);
        final var params = parameterInfoDelegator.create(
                method.getSimpleName().toString(),
                method.getParameters()
        );

        final Map<String, List<ParamInfo>> classPropertyMap =
                (params.size() == 1 && params.getFirst() instanceof ClassParamInfo classParamInfo) ?
                ParamPathExtractor.build(classParamInfo) :
                Collections.emptyMap();

        final var commandOptional = Optional.ofNullable(method.getAnnotation(Command.class))
                .map(command -> {
                    final var packException = typeUtil.getTypeMirrorFromClass(command::packException);
                    final var methodInfo = WriteMethodInfoFactory.create(
                            method,
                            params,
                            classPropertyMap,
                            command,
                            packException
                    );
                    switch (command.commandType()){
                        case INSERT, UPDATE -> {
                            final List<TypeMirror> validReturns = classPropertyMap.isEmpty() ?
                                    List.of(types.getNoType(TypeKind.VOID)) :
                                    List.of(types.getNoType(TypeKind.VOID), params.getFirst().getType());
                            methodValidator.validateReturn(
                                    method.getSimpleName().toString(),
                                    command.returnRowsAffected(),
                                    methodInfo.getReturnType(),
                                    command.commandType().name(),
                                    validReturns);
                        }
                        case DELETE ->
                            methodValidator.validateReturn(
                                    method.getSimpleName().toString(),
                                    command.returnRowsAffected(),
                                    methodInfo.getReturnType(),
                                    command.commandType().name(),
                                    List.of(types.getNoType(TypeKind.VOID))
                            );
                    }
                    return  methodInfo;
                });

        return Optional.ofNullable(method.getAnnotation(Query.class))
                .map(query -> readMethodInfoFactory.create(
                        method,
                        params,
                        classPropertyMap,
                        query,
                        typeUtil.getTypeMirrorFromClass(query::packException)
                ))
                .or(() -> commandOptional)
                .orElseThrow(() -> {
                    final var message = String.format("Fail to get info from method %s", method.getSimpleName());
                    return new InvalidMethodSignatureException(message);
                });
    }

    private ParameterInfoDelegator buildParameterInfoDelegator(final Types types,
                                                               final Elements elements) {
        final var arrayUtil = buildArrayUtil(types, elements);
        final var collectionUtil = buildCollectionUtil(types);
        final var typeUtil = buildTypeUtil(types, elements);
        if (isNull(parameterInfoDelegatorCache)) {
            parameterInfoDelegatorCache =  new ParameterInfoDelegator(
                    new SimpleParamInfoFactory(types, elements, arrayUtil, collectionUtil, typeUtil),
                    new ClassParamInfoFactory(types, elements, arrayUtil, collectionUtil, typeUtil),
                    arrayUtil,
                    collectionUtil,
                    typeUtil
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
                    new SelectResultSimpleResult(),
                    new SelectResultSimpleResultList()
            );
            this.daoGeneratorCache = new DAOGenerator(
                    types,
                    new SelectCollectionMethodGenerator(types, selectResultSetDelegator, statementBuilder, collectionUtil),
                    new SelectOptionalMethodGenerator(types, selectResultSetDelegator, statementBuilder),
                    new SelectSingleMethodGenerator(types, selectResultSetDelegator, statementBuilder),
                    new InsertMethodGenerator(statementBuilder),
                    new UpdateMethodGenerator(statementBuilder),
                    new DeleteMethodGenerator(statementBuilder),
                    collectionUtil,
                    typeUtil
            );
        }
        return daoGeneratorCache;
    }

    @Nullable
    private ArrayUtil arrayUtil;

    private ArrayUtil buildArrayUtil(final Types types, final Elements elements) {
        if (isNull(arrayUtil)) {
            arrayUtil = new ArrayUtil(types, buildTypeUtil(types, elements));
        }
        return arrayUtil;
    }

    @Nullable
    private CollectionUtil collectionUtil;

    public CollectionUtil buildCollectionUtil(final Types types) {
        if (isNull(collectionUtil)) {
            collectionUtil = new CollectionUtil(types);
        }
        return collectionUtil;
    }

    @Nullable
    private TypeUtil typeUtil;

    private TypeUtil buildTypeUtil(final Types types, final Elements elements) {
        if (isNull(typeUtil)) {
            typeUtil = new TypeUtil(elements, types, buildCollectionUtil(types));
        }
        return typeUtil;
    }

    private ReadMethodInfoFactory buildReadMethodInfoFactory(final Types types,
                                                             final Elements elements) {
        final var typeUtil = buildTypeUtil(types, elements);
        final var collectionUtil = buildCollectionUtil(types);
        final var buildConstructorStrategy = new BuildConstructorStrategy(types, typeUtil, collectionUtil);
        final var buildSetterStrategy = new BuildSetterStrategy(types, typeUtil, collectionUtil);
        return new ReadMethodInfoFactory(
                types,
                elements,
                buildConstructorStrategy,
                buildSetterStrategy,
                typeUtil,
                collectionUtil
        );
    }

    private MethodValidator buildMethodValidator(final Elements elements, final Types types){
        return new MethodValidator(elements, types);
    }

    private void writeClass(final JavaFile javaFile, final Messager messager, final Filer filer){
        try {
            javaFile.writeTo(filer);
        }catch (IOException ex){
            messager.printError(ex.getMessage());
        }
    }

}
