package br.com.jdbcpp.processor.facade;

import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.api.Query;
import br.com.jdbcpp.processor.dto.method.InsertMethod;
import br.com.jdbcpp.processor.dto.method.SelectNullableMethodInfo;
import br.com.jdbcpp.processor.exception.InvalidDAOException;
import br.com.jdbcpp.processor.exception.ReadDAOFacadeException;
import br.com.jdbcpp.processor.service.DAOGenerator;
import br.com.jdbcpp.processor.service.constructor.ConstructorFactory;
import br.com.jdbcpp.processor.service.method.MethodInfoDelegator;
import br.com.jdbcpp.processor.service.validation.DAOValidator;
import com.palantir.javapoet.JavaFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.extension.Fixture;
import util.extension.FixtureElement;
import util.extension.MicroProcessorExtension;
import util.extension.ProcessingEnv;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, MicroProcessorExtension.class})
@Fixture(
        resourcePath = "facade/ProcessorFacadeTest.txt",
        packageName = "com.example"
)
class ProcessorFacadeTest {

    @Mock
    private RoundEnvironment roundEnv;
    @Mock
    private Elements elements;
    @Mock
    private DAOValidator daoValidator;
    @Mock
    private ConstructorFactory constructorFactory;
    @Mock
    private MethodInfoDelegator methodInfoDelegator;
    @Mock
    private DAOGenerator daoGenerator;
    @Mock
    private Filer filer;

    @ProcessingEnv
    private ProcessingEnvironment processingEnv;
    @FixtureElement
    private TypeElement fixture;

    @Test
    void shouldProcessDAOSuccessfully() throws Exception {
        final var daoElement = fixture;
        final var facade = createFacade();

        doReturn(Set.of(daoElement)).when(roundEnv).getElementsAnnotatedWith(DAO.class);
        when(daoValidator.validateAndResolve(daoElement)).thenReturn(Optional.empty());
        when(methodInfoDelegator.build(any())).thenAnswer(invocation -> {
            if (isNull(invocation.getArgument(0, ExecutableElement.class).getAnnotation(Query.class))){
                return mock(SelectNullableMethodInfo.class);
            } else {
                return  mock(InsertMethod.class);
            }
        });
        final var javaFile = mock(JavaFile.class);
        when(daoGenerator.build(any())).thenReturn(javaFile);
        doNothing().when(javaFile).writeTo(filer);

        facade.process();

        verify(daoValidator).validateAndResolve(daoElement);
        verify(methodInfoDelegator, times(2)).build(any());
        verify(daoGenerator).build(any());
    }

    @Test
    void shouldThrowExceptionWhenNoDAOsFound() {
        final var facade = createFacade();

        when(roundEnv.getElementsAnnotatedWith(DAO.class)).thenReturn(Set.of());

        assertThatThrownBy(facade::process)
                .isInstanceOf(ReadDAOFacadeException.class)
                .hasMessageContaining("No DAOs found");
    }

    @Test
    void shouldThrowExceptionWhenDAOHasNoAnnotatedMethods() throws InvalidDAOException {
        final var daoElement = processingEnv.getElementUtils()
                .getTypeElement("com.example.ProcessorFacadeTest.ProcessorFacadeTestNoMethods");
        final var facade = createFacade();
        doReturn(Set.of(daoElement)).when(roundEnv).getElementsAnnotatedWith(DAO.class);
        when(daoValidator.validateAndResolve(daoElement)).thenReturn(Optional.empty());

        assertThatThrownBy(facade::process)
                .isInstanceOf(InvalidDAOException.class)
                .hasMessage(
                        "DAO interface com.example.ProcessorFacadeTest.ProcessorFacadeTestNoMethods must have at least one method annotated with @Query or @Commnad"
                );
    }

    private ProcessorFacade createFacade() {
        return new ProcessorFacade(
                roundEnv,
                processingEnv.getElementUtils(),
                daoValidator,
                constructorFactory,
                methodInfoDelegator,
                daoGenerator,
                filer
        );
    }
}
