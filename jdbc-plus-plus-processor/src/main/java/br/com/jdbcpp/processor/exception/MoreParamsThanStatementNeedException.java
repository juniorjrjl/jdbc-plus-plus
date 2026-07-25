package br.com.jdbcpp.processor.exception;

import javax.lang.model.element.Element;

public class MoreParamsThanStatementNeedException extends JDBCPlusPlusProcessorException {


    public MoreParamsThanStatementNeedException(final String message, final Element element) {
        super(message, element);
    }


}
