package br.com.jdbcpp.processor.util;

@FunctionalInterface
public interface ThrowableFunction<T, R, E extends Exception> {

    R apply(final T t) throws E;

}
