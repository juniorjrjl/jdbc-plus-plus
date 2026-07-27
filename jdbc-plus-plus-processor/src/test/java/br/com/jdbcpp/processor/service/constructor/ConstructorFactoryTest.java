package br.com.jdbcpp.processor.service.constructor;

import br.com.jdbcpp.processor.util.ArrayUtil;
import br.com.jdbcpp.processor.util.CollectionUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.FieldUtil;
import util.MicroProcessor;

import javax.lang.model.util.ElementFilter;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ConstructorFactoryTest {

    @Mock
    private ArrayUtil arrayUtil;
    @Mock
    private CollectionUtil collectionUtil;


    @Test
    void shouldReturnNullWhenConstructorIsNull() {
        final var microProcessor = new MicroProcessor<>(
                "service/constructor/ConstructorFactoryTest.txt",
                "com.example",
                processingEnv -> new ConstructorFactory(
                        arrayUtil,
                        collectionUtil,
                        processingEnv.getTypeUtils()
                )
        );
        microProcessor.compile((testInstance, fixture) ->
                assertThat(testInstance.build(null)).isNull()
        );
        verifyNoInteractions(arrayUtil, collectionUtil);
    }

    @Test
    void shouldObtainConstructorInfo() {
        final var microProcessor = new MicroProcessor<>(
                "service/constructor/ConstructorFactoryTest.txt",
                "com.example",
                processingEnv -> new ConstructorFactory(
                        arrayUtil,
                        collectionUtil,
                        processingEnv.getTypeUtils()
                )
        );
        microProcessor.compile((testInstance, fixture) ->{
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

            final var actual = testInstance.build(constructor);

            expectedFields.forEach((fieldName, expectedType) -> {
                final var foundParam = actual.params().stream()
                        .filter(p -> p.name().equals(fieldName))
                        .findFirst()
                        .orElseThrow();
                assertThat(foundParam.type().toString()).isEqualTo(expectedType);
            });
        });
    }

}