package br.com.jdbcpp.processor.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class LambdaUtil {

    private LambdaUtil() {}

    public static <T, R, E extends Exception> Function<T, R> unchecked(final ThrowableFunction<T, R, E> function) {
        return t ->{
            try{
                return function.apply(t);
            } catch (final Exception e) {
                throw sneakyThrow(e);
            }
        };
    }

    public static <T, E extends Exception> Supplier<T> unchecked(final ThrowableSupplier<T, E> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                throw sneakyThrow(e);
            }
        };
    }

    public static <T, E extends Exception> Consumer<T> unchecked(final ThrowableConsumer<T, E> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception e) {
                throw sneakyThrow(e);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> E sneakyThrow(final Throwable e) throws E {
        throw (E) e;
    }

}
