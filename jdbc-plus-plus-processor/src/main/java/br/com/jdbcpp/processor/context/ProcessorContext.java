package br.com.jdbcpp.processor.context;

import br.com.jdbcpp.api.Command;
import br.com.jdbcpp.api.Query;
import br.com.jdbcpp.processor.exception.ProcessorContextInitialization;
import br.com.jdbcpp.processor.facade.ProcessorFacade;
import br.com.jdbcpp.processor.service.DAOGenerator;
import br.com.jdbcpp.processor.service.constructor.ConstructorFactory;
import br.com.jdbcpp.processor.service.dao.read.select.SelectCollectionMethodGenerator;
import br.com.jdbcpp.processor.service.dao.read.select.SelectOptionalMethodGenerator;
import br.com.jdbcpp.processor.service.dao.read.select.SelectSingleMethodGenerator;
import br.com.jdbcpp.processor.service.dao.read.select.result.SelectResultSetDelegator;
import br.com.jdbcpp.processor.service.dao.read.select.result.SelectResultSimpleResult;
import br.com.jdbcpp.processor.service.dao.read.select.result.SelectResultSimpleResultList;
import br.com.jdbcpp.processor.service.dao.read.select.result.SelectResultUsingConstructor;
import br.com.jdbcpp.processor.service.dao.read.select.result.SelectResultUsingSetter;
import br.com.jdbcpp.processor.service.dao.statement.StatementBuilder;
import br.com.jdbcpp.processor.service.dao.write.delete.DeleteMethodGenerator;
import br.com.jdbcpp.processor.service.dao.write.insert.InsertMethodGenerator;
import br.com.jdbcpp.processor.service.dao.write.update.UpdateMethodGenerator;
import br.com.jdbcpp.processor.service.method.BuildConstructorStrategy;
import br.com.jdbcpp.processor.service.method.BuildSetterStrategy;
import br.com.jdbcpp.processor.service.method.MethodInfoDelegator;
import br.com.jdbcpp.processor.service.method.ReadMethodInfoFactory;
import br.com.jdbcpp.processor.service.method.WriteMethodInfoFactory;
import br.com.jdbcpp.processor.service.parameter.ClassParamInfoFactory;
import br.com.jdbcpp.processor.service.parameter.ParamPathExtractor;
import br.com.jdbcpp.processor.service.parameter.ParameterInfoDelegator;
import br.com.jdbcpp.processor.service.parameter.SimpleParamInfoFactory;
import br.com.jdbcpp.processor.service.validation.DAOValidator;
import br.com.jdbcpp.processor.service.validation.MethodValidator;
import br.com.jdbcpp.processor.util.ArrayUtil;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.LambdaUtil;
import br.com.jdbcpp.processor.util.TypeUtil;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Optional;

public class ProcessorContext {

    private static final String DATA_SOURCE_CANONICAL_NAME = DataSource.class.getCanonicalName();
    private static final String SQL_EXCEPTION_CANONICAL_NAME = SQLException.class.getCanonicalName();
    private static final String NULL_WRITE_EXCEPTION_CANONICAL_NAME = Command.None.class.getCanonicalName();
    private static final String NULL_READ_EXCEPTION_CANONICAL_NAME = Query.None.class.getCanonicalName();
    private static final String THROWABLE_CANONICAL_NAME = Throwable.class.getCanonicalName();
    private static final String LONG_CANONICAL_NAME = Long.class.getCanonicalName();
    private static final String INTEGER_CANONICAL_NAME = Integer.class.getCanonicalName();

    private final ProcessorFacade processorFacade;

    public ProcessorContext(final RoundEnvironment roundEnvironment,
                            final ProcessingEnvironment processingEnvironment) throws ProcessorContextInitialization {
        final var types = processingEnvironment.getTypeUtils();
        final var elements = processingEnvironment.getElementUtils();
        final var filer = processingEnvironment.getFiler();

        final var dataSourceElement = Optional.ofNullable(elements.getTypeElement(DATA_SOURCE_CANONICAL_NAME))
                .map(TypeElement::asType)
                .orElseThrow(LambdaUtil.unchecked(() -> new ProcessorContextInitialization("DataSource not found")));
        final var sqlExceptionElement = Optional.ofNullable(elements.getTypeElement(SQL_EXCEPTION_CANONICAL_NAME))
                .map(TypeElement::asType)
                .orElseThrow(LambdaUtil.unchecked(() -> new ProcessorContextInitialization("SQLException not found")));
        final var nullWriteException = Optional.ofNullable(elements.getTypeElement(NULL_WRITE_EXCEPTION_CANONICAL_NAME))
                .map(TypeElement::asType)
                .orElseThrow(LambdaUtil.unchecked(() -> new ProcessorContextInitialization("NullWriteException not found")));
        final var nullReadException = Optional.ofNullable(elements.getTypeElement(NULL_READ_EXCEPTION_CANONICAL_NAME))
                .map(TypeElement::asType)
                .orElseThrow(LambdaUtil.unchecked(() -> new ProcessorContextInitialization("NullReadException not found")));
        final var throwableElement = Optional.ofNullable(elements.getTypeElement(THROWABLE_CANONICAL_NAME))
                .map(TypeElement::asType)
                .orElseThrow(LambdaUtil.unchecked(() -> new ProcessorContextInitialization("Throwable not found")));
        final var longElement = Optional.ofNullable(elements.getTypeElement(LONG_CANONICAL_NAME))
                .map(TypeElement::asType)
                .orElseThrow(LambdaUtil.unchecked(() -> new ProcessorContextInitialization("Long not found")));
        final var integerElement = Optional.ofNullable(elements.getTypeElement(INTEGER_CANONICAL_NAME))
                .map(TypeElement::asType)
                .orElseThrow(LambdaUtil.unchecked(() -> new ProcessorContextInitialization("Integer not found")));


        final var daoValidator = new DAOValidator(types, elements, DATA_SOURCE_CANONICAL_NAME, dataSourceElement);

        final var collectionUtil = new CollectionUtil(types);
        final var typeUtil = new TypeUtil(elements, types, collectionUtil);
        final var arrayUtil = new ArrayUtil(typeUtil);

        final var constructorFactory = new ConstructorFactory(arrayUtil, collectionUtil, types);

        final var simpleParamInfoFactory = new SimpleParamInfoFactory(
                types,
                elements,
                arrayUtil,
                collectionUtil,
                typeUtil
        );
        final var classParamInfoFactory =  new ClassParamInfoFactory(
                types,
                elements,
                arrayUtil,
                collectionUtil,
                typeUtil
        );
        final var parameterInfoDelegator = new ParameterInfoDelegator(
                simpleParamInfoFactory,
                classParamInfoFactory,
                arrayUtil,
                collectionUtil,
                typeUtil
        );

        final var statementBuilder = new StatementBuilder();
        final var selectResultSetDelegator = new SelectResultSetDelegator(
                new SelectResultUsingConstructor(),
                new SelectResultUsingSetter(),
                new SelectResultSimpleResult(),
                new SelectResultSimpleResultList()
        );
        final var daoGenerator = new DAOGenerator(
                new SelectCollectionMethodGenerator(selectResultSetDelegator, statementBuilder, collectionUtil, sqlExceptionElement),
                new SelectOptionalMethodGenerator(selectResultSetDelegator, statementBuilder, sqlExceptionElement),
                new SelectSingleMethodGenerator(selectResultSetDelegator, statementBuilder, sqlExceptionElement),
                new InsertMethodGenerator(statementBuilder, sqlExceptionElement),
                new UpdateMethodGenerator(statementBuilder, sqlExceptionElement),
                new DeleteMethodGenerator(statementBuilder, sqlExceptionElement),
                collectionUtil,
                typeUtil
        );

        final var methodValidator = new MethodValidator(
                types,
                longElement,
                integerElement,
                throwableElement,
                sqlExceptionElement
        );

        final var buildConstructorStrategy = new BuildConstructorStrategy(
                types,
                typeUtil,
                collectionUtil
        );
        final var buildSetterStrategy = new BuildSetterStrategy(
                types,
                typeUtil,
                collectionUtil
        );
        final var readMethodInfoFactory = new ReadMethodInfoFactory(
                types,
                buildConstructorStrategy,
                buildSetterStrategy,
                typeUtil,
                methodValidator,
                collectionUtil,
                nullReadException,
                sqlExceptionElement
        );

        final var writeMethodInfoFactory = new WriteMethodInfoFactory(
                methodValidator,
                sqlExceptionElement,
                nullWriteException,
                types
        );

        final var paramPathExtractor = new ParamPathExtractor();

        final var methodInfoDelegator = new MethodInfoDelegator(
                parameterInfoDelegator,
                paramPathExtractor,
                writeMethodInfoFactory,
                methodValidator,
                readMethodInfoFactory,
                typeUtil,
                types
        );

        this.processorFacade = new ProcessorFacade(
                roundEnvironment,
                elements,
                daoValidator,
                constructorFactory,
                methodInfoDelegator,
                daoGenerator,
                filer
        );

    }

    public ProcessorFacade getProcessorFacade() {
        return processorFacade;
    }

}
