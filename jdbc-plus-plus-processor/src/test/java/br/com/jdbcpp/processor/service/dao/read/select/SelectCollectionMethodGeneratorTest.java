package br.com.jdbcpp.processor.service.dao.read.select;

import br.com.jdbcpp.api.ResultBuildStrategyType;
import br.com.jdbcpp.processor.dto.method.DeleteMethod;
import br.com.jdbcpp.processor.dto.method.InsertMethod;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.method.QueryType;
import br.com.jdbcpp.processor.dto.method.SelectCollectionMethodInfo;
import br.com.jdbcpp.processor.dto.method.SelectNullableMethodInfo;
import br.com.jdbcpp.processor.dto.method.SelectOptionalMethodInfo;
import br.com.jdbcpp.processor.dto.method.UpdateMethod;
import br.com.jdbcpp.processor.dto.parameter.ClassParamInfo;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfo;
import br.com.jdbcpp.processor.dto.result.ConstructorStrategy;
import br.com.jdbcpp.processor.dto.result.SelectReturnStrategy;
import br.com.jdbcpp.processor.dto.result.SetterStrategy;
import br.com.jdbcpp.processor.dto.result.SimpleResultStrategy;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import br.com.jdbcpp.processor.service.dao.read.select.param.SelectCollectionMethodGeneratorArgumentProvider;
import br.com.jdbcpp.processor.service.dao.read.select.result.SelectResultSetDelegator;
import br.com.jdbcpp.processor.service.dao.read.select.result.SelectResultSimpleResult;
import br.com.jdbcpp.processor.service.dao.read.select.result.SelectResultSimpleResultList;
import br.com.jdbcpp.processor.service.dao.read.select.result.SelectResultUsingConstructor;
import br.com.jdbcpp.processor.service.dao.read.select.result.SelectResultUsingSetter;
import br.com.jdbcpp.processor.service.dao.statement.StatementBuilder;
import br.com.jdbcpp.processor.service.parameter.ParamPathExtractor;
import br.com.jdbcpp.processor.util.CollectionUtil;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.FieldSource;
import org.mockito.junit.jupiter.MockitoExtension;
import util.extension.Fixture;
import util.extension.MicroProcessorExtension;
import util.extension.ProcessingEnv;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static br.com.jdbcpp.processor.dto.ParamKind.JAVA_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith({MockitoExtension.class, MicroProcessorExtension.class})
@Fixture(
        resourcePath = "service/dao/read/select/SelectCollectionMethodGeneratorTest.txt",
        packageName = "com.example"
)
class SelectCollectionMethodGeneratorTest {

    private final StatementBuilder statementBuilder = new StatementBuilder();
    private final SelectResultUsingConstructor selectResultUsingConstructor = new SelectResultUsingConstructor();
    private final SelectResultUsingSetter selectResultUsingSetter = new SelectResultUsingSetter();
    private final SelectResultSimpleResult selectResultSimpleResult = new SelectResultSimpleResult();
    private final SelectResultSimpleResultList selectResultSimpleResultList = new SelectResultSimpleResultList();
    private final SelectResultSetDelegator selectResultSetDelegator = new SelectResultSetDelegator(
            selectResultUsingConstructor,
            selectResultUsingSetter,
            selectResultSimpleResult,
            selectResultSimpleResultList
    );

    @ProcessingEnv
    private ProcessingEnvironment processingEnv;

    @ParameterizedTest
    @ArgumentsSource(SelectCollectionMethodGeneratorArgumentProvider.class)
    void shouldBuildSelectCollectionMethodWithBasicConfiguration(final String returnTypeStr,
                                                                final ResultBuildStrategyType strategyType,
                                                                final boolean unParameterizedStatement,
                                                                final boolean useCustomException,
                                                                final String expectedCode) {
        final var sqlException = processingEnv.getElementUtils()
                .getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var packException = useCustomException
                ? processingEnv.getElementUtils().getTypeElement("java.lang.RuntimeException").asType()
                : sqlException;
        final var collectionUtil = new CollectionUtil(processingEnv.getTypeUtils());
        final var generator = createSelectCollectionMethodGenerator(sqlException, collectionUtil);

        final var strategies = createStrategies(strategyType);
        
        final var methodInfo = createSelectCollectionMethodBuilder(
                returnTypeStr,
                Collections.emptyList(),
                new StatementInfo(List.of("SELECT * FROM users"), List.of()),
                packException,
                strategies,
                strategyType,
                unParameterizedStatement
        ).build();

        final var result = generator.build(methodInfo, "getConnection()").build();

        assertThat(result.toString()).isEqualToNormalizingNewlines(expectedCode);
    }

    private static final List<Arguments> shouldUseInstanceForSelectCollectionMethodInfo =
            List.of(
                    Arguments.of(SelectNullableMethodInfo.class, false),
                    Arguments.of(SelectOptionalMethodInfo.class, false),
                    Arguments.of(SelectCollectionMethodInfo.class, true),
                    Arguments.of(InsertMethod.class, false),
                    Arguments.of(UpdateMethod.class, false),
                    Arguments.of(DeleteMethod.class, false)
            );

    @ParameterizedTest
    @FieldSource
    void shouldUseInstanceForSelectCollectionMethodInfo(final Class<? extends MethodInfo> methodInfoClass,
                                                        final boolean expectedResult) {
        final var sqlException = processingEnv.getElementUtils()
                .getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var collectionUtil = new CollectionUtil(processingEnv.getTypeUtils());
        final var generator = createSelectCollectionMethodGenerator(sqlException, collectionUtil);

        final var selectCollectionMethodInfo = mock(methodInfoClass);
        assertThat(generator.useInstance(selectCollectionMethodInfo)).isEqualTo(expectedResult);
    }

    private SelectCollectionMethodGenerator createSelectCollectionMethodGenerator(final TypeMirror sqlException,
                                                                                 final CollectionUtil collectionUtil) {
        return new SelectCollectionMethodGenerator(selectResultSetDelegator, statementBuilder, collectionUtil, sqlException);
    }

    private List<SelectReturnStrategy<?>> createStrategies(final ResultBuildStrategyType strategyType) {
        final var returnType = processingEnv.getElementUtils().getTypeElement(String.class.getCanonicalName()).asType();
        
        return switch (strategyType) {
            case CONSTRUCTOR -> List.of(new ConstructorStrategy("id", returnType, JAVA_TYPE, List.of(), null, null));
            case SETTER -> List.of(new SetterStrategy("setId", "id", returnType, JAVA_TYPE, List.of(), null, null));
            case SIMPLE_RESULT -> List.of(new SimpleResultStrategy(returnType, null));
        };
    }

    private SelectCollectionMethodInfo.SelectCollectionMethodInfoBuilder createSelectCollectionMethodBuilder(
            final String returnTypeStr,
            final List<ParamInfo> params,
            final StatementInfo statementInfo,
            final TypeMirror packException,
            final List<SelectReturnStrategy<?>> strategies,
            final ResultBuildStrategyType strategyType,
            final boolean unParameterizedStatement) {

        final var returnType = processingEnv.getElementUtils().getTypeElement(returnTypeStr).asType();
        final var listType = processingEnv.getElementUtils().getTypeElement(List.class.getCanonicalName());

        final var listUser = processingEnv.getTypeUtils().getDeclaredType(listType, returnType);

        
        final var classPropertyMap = unParameterizedStatement ?
                Map.<String, List<ParamInfo>>of() :
                createUserClassPropertyMap(returnType);
        
        return MethodInfo.builder()
                .withName("selectUser")
                .withReturnType(returnType)
                .withParams(params)
                .withClassPropertyMap(classPropertyMap)
                .withStatement(statementInfo)
                .withPackException(packException)
                .<SelectCollectionMethodInfo.SelectCollectionMethodInfoBuilder>asReadType(QueryType.COLLECTION)
                .withStrategies(strategies)
                .withStrategyType(strategyType)
                .withContainerReturnTypeMirror(listUser)
                .withInstanceContainer(listUser);
    }

    private Map<String, List<ParamInfo>> createUserClassPropertyMap(final TypeMirror userType) {
        final var stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
        
        final var idParam = SimpleParamInfo.builder()
                .withName("id")
                .withType(stringType)
                .withCustomEnum(false)
                .withQueryParamName("id")
                .withConvertMethod("")
                .build();
        
        final var userClassParam = ClassParamInfo.builder()
                .withName("user")
                .withType(userType)
                .withContainerType(null)
                .withNestedProperties(List.of(idParam))
                .withRecordClass(false)
                .withConvertMethod("")
                .build();
        
        return new ParamPathExtractor().build(userClassParam);
    }
}
