package br.com.jdbcpp.processor.service.validation;

import br.com.jdbcpp.processor.exception.InvalidDAOException;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Optional;

public class DAOValidator {

    private final Types types;
    private final Elements elements;
    private final String dataSourceCanonicalName;
    private final TypeMirror dataSource;

    public DAOValidator(final Types types,
                        final Elements elements,
                        final String dataSourceCanonicalName,
                        final TypeMirror dataSource) {
        this.types = types;
        this.elements = elements;
        this.dataSourceCanonicalName = dataSourceCanonicalName;
        this.dataSource = dataSource;
    }

    public Optional<ExecutableElement> validateAndResolve(final Element mappedDAO) throws InvalidDAOException {
        if (mappedDAO.getKind() == ElementKind.INTERFACE){
            return Optional.empty();
        }

        if (mappedDAO.getKind() != ElementKind.CLASS){
            throw new IllegalArgumentException("Invalid Element: " + mappedDAO.getKind());
        }

        final var className = elements.getTypeElement(mappedDAO.toString()).toString();
        checkIfClassIsAbstract(mappedDAO, className);
        checkIfHasDataSource(mappedDAO, className);
        final var constructor = getConstructorWithDataSource(mappedDAO, className);

        return Optional.of(constructor);
    }

    private ExecutableElement getConstructorWithDataSource(final Element mappedDAO,
                                                           final String className) throws InvalidDAOException {
        final var message = String.format(
                "Invalid DAO %s: For DAO abstract classes is required exactly one constructor with a param type %s",
                dataSourceCanonicalName,
                className
        );
        return mappedDAO.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
                .map(ExecutableElement.class::cast)
                .filter(e -> e.getModifiers().contains(Modifier.PUBLIC) || e.getModifiers().contains(Modifier.PROTECTED))
                .filter(c -> c.getParameters().stream().anyMatch(p -> p.asType().equals(dataSource)))
                .findFirst()
                .orElseThrow(() -> new InvalidDAOException(message, mappedDAO));
    }

    private void checkIfHasDataSource(final Element mappedDAO, final String className) throws InvalidDAOException {
        final var expectedModifiers = List.of(Modifier.PROTECTED, Modifier.FINAL);
        final var message = String.format(
                "Invalid DAO %s: For DAO abstract classes a protected final field of type DataSource is required",
                className
        );
        mappedDAO.getEnclosedElements().stream()
                .filter(e -> e.getModifiers().containsAll(expectedModifiers))
                .filter(e -> types.isSameType(e.asType(), dataSource))
                .findFirst()
                .orElseThrow(() ->new InvalidDAOException(message, mappedDAO));
    }

    private static void checkIfClassIsAbstract(final Element mappedDAO,
                                               final String className) throws InvalidDAOException {
        if (!mappedDAO.getModifiers().contains(Modifier.ABSTRACT)) {
            final var message = String.format(
                    "Invalid DAO %s: A DAO annotation is used on a abstract classes or interfaces",
                    className
            );
            throw new InvalidDAOException(message, mappedDAO);
        }
    }

}
