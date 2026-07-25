package br.com.jdbcpp.processor.exception;

import javax.lang.model.element.Element;

public class InvalidMethodSignatureException extends JDBCPlusPlusProcessorException {

    public InvalidMethodSignatureException(final String message, final Element element) {
        super(message, element);
    }

}
