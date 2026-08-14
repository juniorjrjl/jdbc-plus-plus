package br.com.jdbcpp.processor.service;

import br.com.jdbcpp.api.CommandType;
import br.com.jdbcpp.processor.dto.DAOImplInfo;
import br.com.jdbcpp.processor.dto.method.InsertMethod;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.statement.StatementInfo;
import br.com.jdbcpp.processor.service.dao.MethodGenerator;
import br.com.jdbcpp.processor.service.dao.read.select.SelectCollectionMethodGenerator;
import br.com.jdbcpp.processor.service.dao.read.select.SelectOptionalMethodGenerator;
import br.com.jdbcpp.processor.service.dao.read.select.SelectSingleMethodGenerator;
import br.com.jdbcpp.processor.service.dao.write.delete.DeleteMethodGenerator;
import br.com.jdbcpp.processor.service.dao.write.insert.InsertMethodGenerator;
import br.com.jdbcpp.processor.service.dao.write.update.UpdateMethodGenerator;
import br.com.jdbcpp.processor.service.param.DAOGeneratorArgumentProvider;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.TypeUtil;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.extension.Fixture;
import util.extension.MicroProcessorExtension;
import util.extension.ProcessingEnv;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static javax.lang.model.element.Modifier.PUBLIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, MicroProcessorExtension.class})
@Fixture(
        resourcePath = "service/DAOGeneratorTest.txt",
        packageName = "com.example"
)
class DAOGeneratorTest {

    @Mock
    private MethodGenerator<MethodInfo> methodGenerator;

    @ProcessingEnv
    private ProcessingEnvironment processingEnv;

    @ParameterizedTest
    @ArgumentsSource(DAOGeneratorArgumentProvider.class)
    void shouldGenerateDAOImplementationWithoutMethods(final Function<Elements, DAOImplInfo> daoCallbackBuilder,
                                                       final String expectedCode) {
        final var generator = new DAOGenerator(List.of(methodGenerator));

        when(methodGenerator.useInstance(any())).thenReturn(true);

        final var mockMethod = MethodSpec.methodBuilder("insertUser")
                .addModifiers(PUBLIC)
                .returns(TypeName.VOID);
        doReturn(mockMethod)
                .when(methodGenerator)
                .build(any(), any());

        final var result = generator.build(daoCallbackBuilder.apply(processingEnv.getElementUtils()));

        assertThat(result.toString()).isEqualToNormalizingNewlines(expectedCode);
    }

    /*@Test
    void shouldGenerateDAOImplementationWithMockedMethods() {
        final var generator = new DAOGenerator(List.of(methodGenerator));

        final var sqlException = processingEnv.getElementUtils()
                .getTypeElement(SQLException.class.getCanonicalName()).asType();
        final var voidType = processingEnv.getElementUtils()
                .getTypeElement("java.lang.Void").asType();

        final var mockMethod = MethodSpec.methodBuilder("insertUser")
                .addModifiers(PUBLIC)
                .returns(TypeName.VOID);

        when(methodGenerator.build(any(), anyString()))
                .thenReturn(mockMethod);

        final var daoInterface = processingEnv.getElementUtils()
                .getTypeElement("com.example.DAOGeneratorTest.DAOInterface");
        final MethodInfo methodInfo = MethodInfo.builder()
                .withName("insertUser")
                .withReturnType(voidType)
                .withParams(Collections.emptyList())
                .withClassPropertyMap(Collections.emptyMap())
                .withStatement(new StatementInfo(List.of("INSERT INTO users"), Collections.emptyList()))
                .withPackException(sqlException)
                .<InsertMethod.InsertMethodBuilder>asWriteType(CommandType.INSERT)
                .withReturnRowsAffected(false)
                .build();

        final var daoImplInfo = DAOImplInfo.builder()
                .name(daoInterface.toString())
                .packageName("com.example")
                .methods(List.of(methodInfo))
                .build();

        final var result = generator.build(daoImplInfo);

        final var expectedCode = """
                package com.example;
                                
                import javax.sql.DataSource;
                                
                public class DAOInterfaceImpl implements DAOGeneratorTest.DAOInterface {
                  private final DataSource dataSource;
                                
                  public DAOInterfaceImpl(final DataSource dataSource) {
                    this.dataSource = dataSource;
                  }
                                
                  public void insertUser() {
                  }
                }
                """;

        assertThat(result.toString()).isEqualToNormalizingNewlines(expectedCode);
    }*/
}
