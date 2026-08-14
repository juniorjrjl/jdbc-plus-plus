package br.com.jdbcpp.processor.service.constructor;

import br.com.jdbcpp.processor.util.ArrayUtil;
import br.com.jdbcpp.processor.util.CollectionUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.FieldUtil;
import util.extension.Fixture;
import util.extension.FixtureElement;
import util.extension.MicroProcessorExtension;
import util.extension.ProcessingEnv;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith({MockitoExtension.class, MicroProcessorExtension.class})
@Fixture(
        resourcePath = "service/constructor/ConstructorFactoryTest.txt",
        packageName = "com.example"
)
class ConstructorFactoryTest {

    @Mock
    private ArrayUtil arrayUtil;
    @Mock
    private CollectionUtil collectionUtil;
    @ProcessingEnv
    private ProcessingEnvironment processingEnv;
    @FixtureElement
    private TypeElement fixture;
    private ConstructorFactory createConstructorFactory(){
        return new ConstructorFactory(
                arrayUtil,
                collectionUtil,
                processingEnv.getTypeUtils()
        );
    }


    @Test
    void shouldReturnNullWhenConstructorIsNull() {
        final var constructorFactory = createConstructorFactory();
        assertThat(constructorFactory.build(null)).isNull();
        verifyNoInteractions(arrayUtil, collectionUtil);
    }

    @Test
    void shouldObtainConstructorInfo() {
        final var constructorFactory = createConstructorFactory();
        final var constructor = ElementFilter.constructorsIn(fixture.getEnclosedElements())
                .getFirst();
        final var array = FieldUtil.getFieldType(fixture, "intArray");
        lenient().when(arrayUtil.isArray(array)).thenReturn(true);


        final var list = FieldUtil.getFieldType(fixture, "stringList");
        lenient().when(collectionUtil.isCollectionType(list)).thenReturn(true);
        final var set = FieldUtil.getFieldType(fixture, "integerSet");
        lenient().when(collectionUtil.isCollectionType(set)).thenReturn(true);

        final var expectedFields = Map.of(
                "stringList", "java.util.List<java.lang.String>",
                "integerSet", "java.util.Set<java.lang.Integer>",
                "intArray", "int[]",
                "primitiveValue", "double",
                "wrapperValue", "java.lang.Boolean"
        );

        final var actual = constructorFactory.build(constructor);

        expectedFields.forEach((fieldName, expectedType) -> {
            assertThat(actual).isNotNull();
            final var foundParam = actual.params().stream()
                    .filter(p -> p.name().equals(fieldName))
                    .findFirst()
                    .orElseThrow();
            assertThat(foundParam.type()).hasToString(expectedType);
        });
    }

}