package br.com.jdbcpp.processor.service.select;

import br.com.jdbcpp.processor.dto.method.SelectMethodInfo;
import br.com.jdbcpp.processor.service.select.result.SelectResultSetDelegator;
import com.palantir.javapoet.MethodSpec;

import javax.lang.model.util.Types;

public abstract class SelectMethodGenerator {

    protected final Types types;
    protected final SelectResultSetDelegator selectResultSetDelegator;

    protected SelectMethodGenerator(final Types types,
                                    final SelectResultSetDelegator selectResultSetDelegator) {
        this.types = types;
        this.selectResultSetDelegator = selectResultSetDelegator;
    }

    public abstract MethodSpec build(final SelectMethodInfo selectMethodInfo);

    protected static String getResultSetGetter(final String typeName) {
        return switch (typeName) {
            case "java.lang.String" -> "String";
            case "java.lang.Integer", "int" -> "Int";
            case "java.lang.Long", "long" -> "Long";
            case "java.lang.Double", "double" -> "Double";
            case "java.lang.Float", "float" -> "Float";
            case "java.lang.Boolean", "boolean" -> "Boolean";
            case "java.util.Date", "java.time.LocalDate", "java.time.LocalDateTime",
                 "java.time.Instant", "java.time.LocalTime" -> "Timestamp";
            default -> "Object";
        };
    }

}
