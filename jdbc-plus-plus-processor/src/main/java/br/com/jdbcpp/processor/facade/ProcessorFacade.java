package br.com.jdbcpp.processor.facade;

import br.com.jdbcpp.api.Command;
import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.api.Query;
import br.com.jdbcpp.processor.dto.DAOImplInfo;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.exception.InvalidDAOException;
import br.com.jdbcpp.processor.exception.JDBCPlusPlusProcessorException;
import br.com.jdbcpp.processor.exception.ReadDAOFacadeException;
import br.com.jdbcpp.processor.service.DAOGenerator;
import br.com.jdbcpp.processor.service.constructor.ConstructorFactory;
import br.com.jdbcpp.processor.service.method.MethodInfoDelegator;
import br.com.jdbcpp.processor.service.validation.DAOValidator;

import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

public class ProcessorFacade {

    private final RoundEnvironment roundEnv;
    private final Elements elements;
    private final DAOValidator daoValidator;
    private final ConstructorFactory constructorFactory;
    private final MethodInfoDelegator methodInfoDelegator;
    private final DAOGenerator daoGenerator;
    private final Filer filer;

    public ProcessorFacade(final RoundEnvironment roundEnv,
                           final Elements elements,
                           final DAOValidator daoValidator,
                           final ConstructorFactory constructorFactory,
                           final MethodInfoDelegator methodInfoDelegator,
                           final DAOGenerator daoGenerator,
                           final Filer filer) {
        this.roundEnv = roundEnv;
        this.elements = elements;
        this.daoValidator = daoValidator;
        this.constructorFactory = constructorFactory;
        this.methodInfoDelegator = methodInfoDelegator;
        this.daoGenerator = daoGenerator;
        this.filer = filer;
    }

    public void process() throws ReadDAOFacadeException,
            JDBCPlusPlusProcessorException,
            IOException {
        final var mappedDAOs = roundEnv.getElementsAnnotatedWith(DAO.class)
                .stream()
                .toList();

        if (mappedDAOs.isEmpty()) {
            throw new ReadDAOFacadeException("No DAOs found");
        }

        for (final var mappedDAO : mappedDAOs) {
            final var packageName = elements.getPackageOf(mappedDAO).toString();
            final var className = elements.getTypeElement(mappedDAO.toString()).toString();
            final var daoImplInfoBuilder = DAOImplInfo.builder().name(className).packageName(packageName);
            final var methods = ElementFilter.methodsIn(mappedDAO.getEnclosedElements()).stream()
                    .filter(m -> nonNull(m.getAnnotation(Query.class)) || nonNull(m.getAnnotation(Command.class)))
                    .toList();

            daoValidator.validateAndResolve(mappedDAO)
                    .map(constructorFactory::build)
                    .ifPresent(daoImplInfoBuilder::constructor);

            if (methods.isEmpty()) {
                final var message = String.format(
                        "DAO interface %s must have at least one method annotated with @Query or @Commnad",
                        className
                );
                throw new InvalidDAOException(message, mappedDAO);
            }

            final List<MethodInfo> methodsInfo = new ArrayList<>();
            for(final var method: methods){
                final var methodInfo = methodInfoDelegator.build(method);
                methodsInfo.add(methodInfo);
            }

            final var daoImplInfo = daoImplInfoBuilder.methods(methodsInfo).build();
            final var javaFile = daoGenerator.build(daoImplInfo);
            javaFile.writeTo(filer);

        }

    }

}
