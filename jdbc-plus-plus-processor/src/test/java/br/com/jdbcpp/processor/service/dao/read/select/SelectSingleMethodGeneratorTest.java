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
import br.com.jdbcpp.processor.service.dao.read.select.param.SelectSingleMethodGeneratorArgumentProvider;
import br.com.jdbcpp.processor.service.dao.read.select.result.SelectResultSetDelegator;
import br.com.jdbcpp.processor.service.dao.read.select.result.SelectResultSimpleResult;
import br.com.jdbcpp.processor.service.dao.read.select.result.SelectResultSimpleResultList;
import br.com.jdbcpp.processor.service.dao.read.select.result.SelectResultUsingConstructor;
import br.com.jdbcpp.processor.service.dao.read.select.result.SelectResultUsingSetter;
import br.com.jdbcpp.processor.service.dao.statement.StatementBuilder;
import br.com.jdbcpp.processor.service.parameter.ParamPathExtractor;
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
        resourcePath = "service/dao/read/select/SelectSingleMethodGeneratorTest.txt",
        packageName = "com.example"
)
class SelectSingleMethodGeneratorTest {

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
    @ArgumentsSource(SelectSingleMethodGeneratorArgumentProvider.class)
    void shouldBuildSelectSingleMethodWithBasicConfiguration(final String returnTypeStr,
                                                             final ResultBuildStrategyType strategyType,
                                                             final boolean unParameterizedStatement,
                                                             final boolean useCustomException,
                                                             final String expectedCode) {
        final var sqlException = processingEnv.getElementUtils()
                .getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var packException = useCustomException
                ? processingEnv.getElementUtils().getTypeElement("java.lang.RuntimeException").asType()
                : sqlException;
        final var generator = createSelectSingleMethodGenerator(sqlException);

        final var strategies = createStrategies(strategyType);
        
        final var methodInfo = createSelectNullableMethodBuilder(
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

    private static final List<Arguments> shouldUseInstanceForSelectNullableMethodInfo =
            List.of(
                    Arguments.of(SelectNullableMethodInfo.class, true),
                    Arguments.of(SelectOptionalMethodInfo.class, false),
                    Arguments.of(SelectCollectionMethodInfo.class, false),
                    Arguments.of(InsertMethod.class, false),
                    Arguments.of(UpdateMethod.class, false),
                    Arguments.of(DeleteMethod.class, false)
            );

    @ParameterizedTest
    @FieldSource
    void shouldUseInstanceForSelectNullableMethodInfo(final Class<? extends MethodInfo> methodInfoClass,
                                                      final boolean expectedResult) {
        final var sqlException = processingEnv.getElementUtils()
                .getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var generator = createSelectSingleMethodGenerator(sqlException);

        final var selectNullableMethodInfo = mock(methodInfoClass);
        assertThat(generator.useInstance(selectNullableMethodInfo)).isEqualTo(expectedResult);
    }

    private SelectSingleMethodGenerator createSelectSingleMethodGenerator(final TypeMirror sqlException) {
        return new SelectSingleMethodGenerator(selectResultSetDelegator, statementBuilder, sqlException);
    }

    private List<SelectReturnStrategy<?>> createStrategies(final ResultBuildStrategyType strategyType) {
        final var returnType = processingEnv.getElementUtils().getTypeElement(String.class.getCanonicalName()).asType();
        
        return switch (strategyType) {
            case CONSTRUCTOR -> List.of(new ConstructorStrategy("id", returnType, JAVA_TYPE, List.of(), null, null));
            case SETTER -> List.of(new SetterStrategy("setId", "id", returnType, JAVA_TYPE, List.of(), null, null));
            case SIMPLE_RESULT -> List.of(new SimpleResultStrategy(returnType, null));
        };
    }

    private SelectNullableMethodInfo.SelectNullableMethodInfoBuilder createSelectNullableMethodBuilder(
            final String returnTypeStr,
            final List<ParamInfo> params,
            final StatementInfo statementInfo,
            final TypeMirror packException,
            final List<SelectReturnStrategy<?>> strategies,
            final ResultBuildStrategyType strategyType,
            final boolean unParameterizedStatement) {
        final var returnType = processingEnv.getElementUtils().getTypeElement(returnTypeStr).asType();
        
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
                .<SelectNullableMethodInfo.SelectNullableMethodInfoBuilder>asReadType(QueryType.NULLABLE)
                .withStrategies(strategies)
                .withStrategyType(strategyType);
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