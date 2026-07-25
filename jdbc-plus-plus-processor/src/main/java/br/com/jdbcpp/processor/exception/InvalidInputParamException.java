package br.com.jdbcpp.processor.exception;

import javax.lang.model.element.Element;

public class InvalidInputParamException extends JDBCPlusPlusProcessorException {

    public InvalidInputParamException(final String message, final Element element) {
        super(message, element);
    }

    public InvalidInputParamException(final String message) {
        super(message);
    }

}
