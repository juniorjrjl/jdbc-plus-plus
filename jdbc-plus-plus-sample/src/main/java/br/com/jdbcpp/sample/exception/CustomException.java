package br.com.jdbcpp.sample.exception;

public class CustomException extends RuntimeException {

    public CustomException(final Throwable cause) {
        super("Custom Exception", cause);
    }

}
