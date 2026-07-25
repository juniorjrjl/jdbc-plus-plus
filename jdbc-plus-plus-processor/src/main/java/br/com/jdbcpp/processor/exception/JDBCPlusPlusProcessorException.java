package br.com.jdbcpp.processor.exception;

import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Element;

public class JDBCPlusPlusProcessorException extends Exception {

    @Nullable
    private final Element element;

    public JDBCPlusPlusProcessorException(final String message, final Element element) {
        super(message);
        this.element = element;
    }

    public JDBCPlusPlusProcessorException(final String message) {
        super(message);
        this.element = null;
    }

    @Nullable
    public Element getElement() {
        return element;
    }

}
