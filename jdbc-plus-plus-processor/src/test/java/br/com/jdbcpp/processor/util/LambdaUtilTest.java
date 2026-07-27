package br.com.jdbcpp.processor.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LambdaUtilTest {

    @Test
    void shouldWrapFunctionThatDoesNotThrow() {
        final var function = LambdaUtil.<String, Integer, Exception>unchecked(Integer::parseInt);

        final var result = function.apply("123");

        assertThat(result).isEqualTo(123);
    }

    @Test
    void shouldWrapFunctionThatThrows() {
        final var function = LambdaUtil.<String, Integer, Exception>unchecked(s -> {
            if (s.equals("invalid")) {
                throw new NumberFormatException("Invalid number");
            }
            return Integer.parseInt(s);
        });

        assertThatThrownBy(() -> function.apply("invalid"))
                .isInstanceOf(NumberFormatException.class)
                .hasMessage("Invalid number");
    }

    @Test
    void shouldWrapSupplierThatDoesNotThrow() {
        final var supplier = LambdaUtil.<String, Exception>unchecked(() -> "test");

        final var result = supplier.get();

        assertThat(result).isEqualTo("test");
    }

    @Test
    void shouldWrapSupplierThatThrows() {
        final var supplier = LambdaUtil.<String, Exception>unchecked(() -> {
            throw new IllegalStateException("Test exception");
        });

        assertThatThrownBy(supplier::get)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Test exception");
    }

}
