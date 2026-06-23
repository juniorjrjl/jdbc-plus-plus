package br.com.jdbcpp.processor.exception;

import javax.lang.model.element.Element;

public class InvalidDAOException extends JDBCPlusPlusProcessorException {

    private final Element element;

    public InvalidDAOException(final String message, final Element element) {
        super(message);
        this.element = element;
    }

    public Element getElement() {
        return element;
    }

}
