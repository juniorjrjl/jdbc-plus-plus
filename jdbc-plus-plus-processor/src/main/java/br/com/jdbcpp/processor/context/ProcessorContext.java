package br.com.jdbcpp.processor.context;

import br.com.jdbcpp.processor.dto.constructor.ConstructorFactory;
import br.com.jdbcpp.processor.dto.method.MethodInfoDelegator;
import br.com.jdbcpp.processor.dto.method.ReadMethodInfoFactory;
import br.com.jdbcpp.processor.dto.method.WriteMethodInfoFactory;
import br.com.jdbcpp.processor.dto.parameter.ClassParamInfoFactory;
import br.com.jdbcpp.processor.dto.parameter.ParamPathExtractor;
import br.com.jdbcpp.processor.dto.parameter.ParameterInfoDelegator;
import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfoFactory;
import br.com.jdbcpp.processor.exception.ProcessorContextInitialization;
import br.com.jdbcpp.processor.facade.ProcessorFacade;
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
import br.com.jdbcpp.processor.service.validation.DAOValidator;
import br.com.jdbcpp.processor.service.write.delete.DeleteMethodGenerator;
import br.com.jdbcpp.processor.service.write.insert.InsertMethodGenerator;
import br.com.jdbcpp.processor.service.write.update.UpdateMethodGenerator;
import br.com.jdbcpp.processor.util.ArrayUtil;
import br.com.jdbcpp.processor.util.BuildConstructorStrategy;
import br.com.jdbcpp.processor.util.BuildSetterStrategy;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.LambdaUtil;
import br.com.jdbcpp.processor.util.MethodValidator;
import br.com.jdbcpp.processor.util.TypeUtil;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.sql.DataSource;
import java.util.Optional;

public class ProcessorContext {

    private static final String DATA_SOURCE_CANONICAL_NAME = DataSource.class.getCanonicalName();

    private final ProcessorFacade processorFacade;

    public ProcessorContext(final RoundEnvironment roundEnvironment,
                            final ProcessingEnvironment processingEnvironment) throws ProcessorContextInitialization {
        final var types = processingEnvironment.getTypeUtils();
        final var elements = processingEnvironment.getElementUtils();
        final var filer = processingEnvironment.getFiler();
        final var dataSourceElement = Optional.ofNullable(elements.getTypeElement(DATA_SOURCE_CANONICAL_NAME))
                .map(TypeElement::asType)
                .orElseThrow(LambdaUtil.unchecked(() -> new ProcessorContextInitialization("DataSource not found")));

        final var daoValidator = new DAOValidator(types, elements, DATA_SOURCE_CANONICAL_NAME);

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
                new SelectCollectionMethodGenerator(selectResultSetDelegator, statementBuilder, collectionUtil),
                new SelectOptionalMethodGenerator(selectResultSetDelegator, statementBuilder),
                new SelectSingleMethodGenerator(selectResultSetDelegator, statementBuilder),
                new InsertMethodGenerator(statementBuilder),
                new UpdateMethodGenerator(statementBuilder),
                new DeleteMethodGenerator(statementBuilder),
                collectionUtil,
                typeUtil
        );

        final var methodValidator = new MethodValidator(elements, types);

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
                elements,
                buildConstructorStrategy,
                buildSetterStrategy,
                typeUtil,
                methodValidator,
                collectionUtil
        );

        final var writeMethodInfoFactory = new WriteMethodInfoFactory(methodValidator);

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
                dataSourceElement,
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
