package br.com.jdbcpp.processor.util;

@FunctionalInterface
public interface ThrowableSupplier<T, E extends Exception> {

    T get() throws E;

}
