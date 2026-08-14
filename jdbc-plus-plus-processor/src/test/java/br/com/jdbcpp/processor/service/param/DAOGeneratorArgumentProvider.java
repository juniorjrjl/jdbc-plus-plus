package br.com.jdbcpp.processor.service.param;

import br.com.jdbcpp.processor.dto.DAOImplInfo;
import br.com.jdbcpp.processor.dto.constructor.ConstructorInfo;
import br.com.jdbcpp.processor.dto.constructor.ConstructorParamInfo;
import br.com.jdbcpp.processor.dto.method.InsertMethod;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

import javax.lang.model.util.Elements;
import javax.sql.DataSource;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

public class DAOGeneratorArgumentProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters,
                                                        final ExtensionContext context) {
        final Function<Elements, DAOImplInfo> daoInterface = e -> DAOImplInfo.builder()
                .name("com.example.DAOGeneratorTest.DAOInterface")
                .packageName("com.example")
                .methods(List.of(mock(InsertMethod.class)))
                .build();

        final Function<Elements, DAOImplInfo> daoClass = e -> DAOImplInfo.builder()
                .name("com.example.DAOGeneratorTest.DAOClass")
                .packageName("com.example")
                .methods(List.of(mock(InsertMethod.class)))
                .constructor(new ConstructorInfo(List.of(
                        new ConstructorParamInfo(
                                "dataSource", e.getTypeElement(DataSource.class.getCanonicalName()).asType()
                        ),
                        new ConstructorParamInfo(
                                "sample",
                                e.getTypeElement(String.class.getCanonicalName()).asType()
                        )
                )))
                .build();

        return Stream.of(
                Arguments.of(
                        daoInterface,
                        """
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
                        """
                ),
                Arguments.of(
                        daoClass,
                        """
                        package com.example;
                                        
                        import java.lang.String;
                        import javax.sql.DataSource;
                                
                        public class DAOClassImpl extends DAOGeneratorTest.DAOClass {
                          public DAOClassImpl(final DataSource dataSource, final String sample) {
                            super(dataSource, sample);
                          }
                          
                          public void insertUser() {
                          }
                        }
                        """
                )
        );
    }
}
