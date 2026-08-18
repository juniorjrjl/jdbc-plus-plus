package br.com.jdbcpp.util.tag;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ TYPE, METHOD })
@Retention(RUNTIME)
@Tag("OracleTest")
public @interface OracleTest {
}
