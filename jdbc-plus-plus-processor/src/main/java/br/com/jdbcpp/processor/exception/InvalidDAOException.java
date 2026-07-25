package br.com.jdbcpp.processor.exception;

import javax.lang.model.element.Element;

public class InvalidDAOException extends JDBCPlusPlusProcessorException {

    public InvalidDAOException(final String message, final Element element) {
        super(message, element);
    }

}
