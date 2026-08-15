package br.com.jdbcpp.processor.util;

@FunctionalInterface
public interface ThrowableConsumer<T, E extends Exception> {

    void accept(T t) throws E;

}
