package br.com.jdbcpp.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

//TODO: add javadoc to configure statement input params
@Retention(SOURCE)
@Target({FIELD, PARAMETER})
public @interface InputParam {

    String value() default "";

    boolean ignore() default false;

    String statementField() default "";

    InputParamEnumStrategy strategy() default InputParamEnumStrategy.ORDINAL;

}
