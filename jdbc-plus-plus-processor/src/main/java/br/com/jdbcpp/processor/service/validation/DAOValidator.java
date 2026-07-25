package br.com.jdbcpp.processor.service.validation;

import br.com.jdbcpp.processor.exception.InvalidDAOException;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;

public class DAOValidator {

    private final Types types;
    private final Elements elements;
    private final String dataSourceCanonicalName;

    public DAOValidator(final Types types,
                        final Elements elements,
                        final String dataSourceCanonicalName) {
        this.types = types;
        this.elements = elements;
        this.dataSourceCanonicalName = dataSourceCanonicalName;
    }

    @Nullable
    public ExecutableElement isValid(final Element mappedDAO,
                                     final TypeMirror dataSourceElement) throws InvalidDAOException {
        final var className = elements.getTypeElement(mappedDAO.toString()).toString();
        if (mappedDAO.getKind() == ElementKind.INTERFACE){
            return null;
        }

        if (mappedDAO.getKind() == ElementKind.CLASS){
            if (!mappedDAO.getModifiers().contains(Modifier.ABSTRACT)) {
                final var message = String.format(
                        "Invalid DAO %s: A DAO annotation is used on a abstract classes or interfaces",
                        className
                );
                throw new InvalidDAOException(message, mappedDAO);
            }
            mappedDAO.getEnclosedElements().stream()
                    .filter(e -> e.getModifiers().containsAll(List.of(Modifier.PROTECTED, Modifier.FINAL)))
                    .filter(e -> types.isSameType(e.asType(), dataSourceElement))
                    .findFirst()
                    .orElseThrow(() -> {
                        final var message = String.format(
                                "Invalid DAO %s: For DAO abstract classes a protected final field of type DataSource is required",
                                className
                        );
                        return new InvalidDAOException(message, mappedDAO);
                    });
            return mappedDAO.getEnclosedElements().stream()
                    .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
                    .map(ExecutableElement.class::cast)
                    .filter(e -> e.getModifiers().contains(Modifier.PUBLIC) || e.getModifiers().contains(Modifier.PROTECTED))
                    .filter(c -> c.getParameters().stream()
                            .anyMatch(p -> p.asType().equals(dataSourceElement)))
                    .findFirst()
                    .orElseThrow(() -> {
                        final var message = String.format(
                                "Invalid DAO %s: For DAO abstract classes is required exactly one constructor with a param type %s",
                                dataSourceCanonicalName,
                                className
                        );
                        return new InvalidDAOException(message, mappedDAO);
                    });
        } else {
            return null;
        }
    }

}
