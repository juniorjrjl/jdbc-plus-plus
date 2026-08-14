package br.com.jdbcpp.processor.service;

import br.com.jdbcpp.processor.dto.DAOImplInfo;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.service.dao.MethodGenerator;
import br.com.jdbcpp.processor.service.param.DAOGeneratorArgumentProvider;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;
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
import java.util.List;
import java.util.function.Function;

import static javax.lang.model.element.Modifier.PUBLIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

}
