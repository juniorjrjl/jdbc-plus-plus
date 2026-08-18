package br.com.jdbcpp.exception;

public class CustomSQLException extends RuntimeException {

    public CustomSQLException(final Throwable cause) {
        super(cause);
    }
}
