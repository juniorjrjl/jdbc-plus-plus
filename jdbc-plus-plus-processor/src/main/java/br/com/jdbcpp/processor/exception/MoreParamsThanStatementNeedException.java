package br.com.jdbcpp.processor.exception;

public class MoreParamsThanStatementNeedException extends JDBCPlusPlusProcessorException {

    public MoreParamsThanStatementNeedException(String message) {
        super(message);
    }

}
