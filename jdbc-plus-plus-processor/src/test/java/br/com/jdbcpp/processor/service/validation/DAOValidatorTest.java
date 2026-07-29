package br.com.jdbcpp.processor.service.validation;

import br.com.jdbcpp.processor.exception.InvalidDAOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import util.extension.Fixture;
import util.extension.FixtureElement;
import util.extension.MicroProcessorExtension;
import util.extension.ProcessingEnv;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.sql.DataSource;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MicroProcessorExtension.class)
@Fixture(
        resourcePath = "service/validation/DAOValidatorTest.txt",
        packageName = "com.example"
)
class DAOValidatorTest {

    @ProcessingEnv
    private ProcessingEnvironment processingEnv;
    @FixtureElement
    private TypeElement fixture;
    private DAOValidator createDAOValidator() {
        return new DAOValidator(
                processingEnv.getTypeUtils(),
                processingEnv.getElementUtils(),
                DataSource.class.getCanonicalName(),
                processingEnv.getElementUtils().getTypeElement(DataSource.class.getCanonicalName()).asType()
                );
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForNonClassElement() {
        final var daoValidator = createDAOValidator();
        final var element = ElementFilter.typesIn(fixture.getEnclosedElements())
                .stream()
                .filter(e -> e.getSimpleName().toString().equals("InvalidEnum"))
                .findFirst()
                .orElseThrow();

        assertThatThrownBy(() -> daoValidator.validateAndResolve(element))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Element");
    }

    private static Stream<Arguments> shouldReturnConstructorForValidAbstractClass() {
        return Stream.of(
                Arguments.of("ValidAbstractDAO", true),
                Arguments.of("ValidAbstractPublicDAO", true),
                Arguments.of("ValidDAOInterface", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldReturnConstructorForValidAbstractClass(final String elementName,
                                                      final boolean hasValue) throws InvalidDAOException {
        final var daoValidator = createDAOValidator();
        final var element = ElementFilter.typesIn(fixture.getEnclosedElements())
                .stream()
                .filter(e -> e.getSimpleName().toString().equals(elementName))
                .findFirst()
                .orElseThrow();

        final var result = daoValidator.validateAndResolve(element);

        assertThat(result.isPresent()).isEqualTo(hasValue);
    }

    private static Stream<Arguments> shouldThrowExceptionForInvalidAbstractClass() {
        return Stream.of(
                Arguments.of("InvalidAbstractClassNotAbstract", "A DAO annotation is used on a abstract classes or interfaces"),
                Arguments.of("InvalidAbstractClassMissingDataSourceField", "protected final field of type DataSource is required"),
                Arguments.of("InvalidAbstractClassMissingConstructor", "required exactly one constructor with a param type"),
                Arguments.of("InvalidAbstractClassConstructorWithoutDataSourceParam", "required exactly one constructor with a param type")
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldThrowExceptionForInvalidAbstractClass(final String elementName, final String expectedMessage) {
        final var daoValidator = createDAOValidator();
        final var element = ElementFilter.typesIn(fixture.getEnclosedElements())
                .stream()
                .filter(e -> e.getSimpleName().toString().equals(elementName))
                .findFirst()
                .orElseThrow();

        assertThatThrownBy(() -> daoValidator.validateAndResolve(element))
                .isInstanceOf(InvalidDAOException.class)
                .hasMessageContaining(expectedMessage);
    }

}