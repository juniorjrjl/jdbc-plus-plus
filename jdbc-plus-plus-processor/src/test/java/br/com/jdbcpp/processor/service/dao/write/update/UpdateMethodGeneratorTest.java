package br.com.jdbcpp.processor.service.dao.write.update;

import br.com.jdbcpp.api.CommandType;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.method.UpdateMethod;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import br.com.jdbcpp.processor.dto.statement.StatementParam;
import br.com.jdbcpp.processor.service.dao.statement.StatementBuilder;
import br.com.jdbcpp.processor.service.dao.write.update.param.UpdateMethodGeneratorArgumentProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
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

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({MockitoExtension.class, MicroProcessorExtension.class})
@Fixture(
        resourcePath = "service/dao/write/update/UpdateMethodGeneratorTest.txt",
        packageName = "com.example"
)
class UpdateMethodGeneratorTest {

    private final StatementBuilder statementBuilder = new StatementBuilder();

    @ProcessingEnv
    private ProcessingEnvironment processingEnv;

    @ParameterizedTest
    @ArgumentsSource(UpdateMethodGeneratorArgumentProvider.class)
    void shouldBuildUpdateMethodWithBasicConfiguration(final String returnTypeStr,
                                                       final boolean returnRowsAffected,
                                                       final boolean useCustomException,
                                                       final String expectedCode) {
        final var sqlException = processingEnv.getElementUtils()
                .getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var packException = useCustomException
                ? processingEnv.getElementUtils().getTypeElement("java.lang.RuntimeException").asType()
                : sqlException;
        final var generator = createUpdateMethodGenerator(sqlException);

        final var methodInfo = createUpdateMethodBuilder(
                returnTypeStr,
                Collections.emptyList(),
                new StatementInfo(List.of("UPDATE users"), List.of()),
                packException
        )
                .withReturnRowsAffected(returnRowsAffected)
                .build();

        final var result = generator.build(methodInfo, "getConnection()").build();

        assertThat(result.toString()).isEqualToNormalizingNewlines(expectedCode);
    }

    @Test
    void shouldBuildUpdateMethodWithParameterMatchingReturnType() {
        final var sqlException = processingEnv.getElementUtils()
                .getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var generator = createUpdateMethodGenerator(sqlException);

        final var longType = processingEnv.getElementUtils().getTypeElement("java.lang.Long").asType();
        final var paramInfo = SimpleParamInfo.builder()
                .withName("id")
                .withType(longType)
                .withCustomEnum(false)
                .withQueryParamName("id")
                .withConvertMethod("id")
                .build();

        final var statementParams = List.of(StatementParam.simple("id"));
        final var statementInfo = new StatementInfo(List.of("UPDATE users where id = ?"), statementParams);
        final var methodInfo = createUpdateMethodBuilder(
                "java.lang.Long",
                List.of(paramInfo),
                statementInfo,
                sqlException
        )
                .withReturnRowsAffected(false)
                .build();

        final var result = generator.build(methodInfo, "getConnection()").build();

        final var expectedCode = """
                public java.lang.Long updateUser(final java.lang.Long id) throws java.sql.SQLException {
                  final var statement = "UPDATE users where id = ?";
                  try (final var conn = getConnection();
                  final var stmt = conn.prepareStatement(statement))
                   {
                    var paramIndex = 1;
                    stmt.setObject(paramIndex++, id);
                    stmt.executeUpdate();
                    return id;
                  } catch (final java.sql.SQLException e) {
                    throw e;
                  }
                }
                """;

        assertThat(result.toString()).isEqualToNormalizingNewlines(expectedCode);
    }

    private UpdateMethodGenerator createUpdateMethodGenerator(final TypeMirror sqlException) {
        return new UpdateMethodGenerator(statementBuilder, sqlException);
    }

    private UpdateMethod.UpdateMethodBuilder createUpdateMethodBuilder(final String returnTypeStr,
                                                                       final List<ParamInfo> params,
                                                                       final StatementInfo statementInfo,
                                                                       final TypeMirror packException) {
        return MethodInfo.builder()
                .withName("updateUser")
                .withReturnType(processingEnv.getElementUtils().getTypeElement(returnTypeStr).asType())
                .withParams(params)
                .withClassPropertyMap(Map.of())
                .withStatement(statementInfo)
                .withPackException(packException)
                .asWriteType(CommandType.UPDATE);
    }
}
