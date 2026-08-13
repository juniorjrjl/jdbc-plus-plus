package br.com.jdbcpp.processor.service.dao.write.delete;

import br.com.jdbcpp.api.CommandType;
import br.com.jdbcpp.processor.dto.method.DeleteMethod;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import br.com.jdbcpp.processor.service.dao.statement.StatementBuilder;
import br.com.jdbcpp.processor.service.dao.write.delete.param.DeleteMethodGeneratorArgumentProvider;
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
        resourcePath = "service/dao/write/delete/DeleteMethodGeneratorTest.txt",
        packageName = "com.example"
)
class DeleteMethodGeneratorTest {

    private final StatementBuilder statementBuilder = new StatementBuilder();

    @ProcessingEnv
    private ProcessingEnvironment processingEnv;

    @ParameterizedTest
    @ArgumentsSource(DeleteMethodGeneratorArgumentProvider.class)
    void shouldBuildDeleteMethodWithBasicConfiguration(final String returnTypeStr,
                                                       final boolean returnRowsAffected,
                                                       final boolean useCustomException,
                                                       final String expectedCode) {
        final var sqlException = processingEnv.getElementUtils()
                .getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var packException = useCustomException
                ? processingEnv.getElementUtils().getTypeElement("java.lang.RuntimeException").asType()
                : sqlException;
        final var generator = createDeleteMethodGenerator(sqlException);

        final var methodInfo = createDeleteMethodBuilder(
                returnTypeStr,
                Collections.emptyList(),
                new StatementInfo(List.of("DELETE FROM users"), List.of()),
                packException
        )
                .withReturnRowsAffected(returnRowsAffected)
                .build();

        final var result = generator.build(methodInfo, "getConnection()").build();

        assertThat(result.toString()).isEqualToNormalizingNewlines(expectedCode);
    }

    private DeleteMethodGenerator createDeleteMethodGenerator(final TypeMirror sqlException) {
        return new DeleteMethodGenerator(statementBuilder, sqlException);
    }

    private DeleteMethod.DeleteMethodBuilder createDeleteMethodBuilder(final String returnTypeStr,
                                                                       final List<ParamInfo> params,
                                                                       final StatementInfo statementInfo,
                                                                       final TypeMirror packException) {
        return MethodInfo.builder()
                .withName("deleteUser")
                .withReturnType(processingEnv.getElementUtils().getTypeElement(returnTypeStr).asType())
                .withParams(params)
                .withClassPropertyMap(Map.of())
                .withStatement(statementInfo)
                .withPackException(packException)
                .asWriteType(CommandType.DELETE);
    }
}
