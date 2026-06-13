package br.com.jdbcpp.processor.service.select.result;

import br.com.jdbcpp.processor.dto.result.SelectReturnStrategy;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public abstract class SelectResultSet<T extends SelectReturnStrategy<T>> {

    public abstract void build(final List<T> strategies,
                               final String objectResultName,
                               final TypeMirror returnType,
                               final String resultSetVar,
                               final MethodSpec.Builder builder);

    protected static String getResultSetGetter(final TypeName type,
                                             final String columnAccessor,
                                             final String resultSetVar) {
        final String typeName = type.toString();
        return switch (typeName) {
            case "int" -> String.format("%s.getInt(%s)", resultSetVar, columnAccessor);
            case "long" -> String.format("%s.getLong(%s)", resultSetVar, columnAccessor);
            case "double" -> String.format("%s.getDouble(%s)", resultSetVar, columnAccessor);
            case "float" -> String.format("%s.getFloat(%s)", resultSetVar, columnAccessor);
            case "boolean" -> String.format("%s.getBoolean(%s)", resultSetVar, columnAccessor);
            case "short" -> String.format("%s.getShort(%s)", resultSetVar, columnAccessor);
            case "byte" -> String.format("%s.getByte(%s)", resultSetVar, columnAccessor);
            case "java.lang.String" -> String.format("%s.getString(%s)", resultSetVar, columnAccessor);
            case "java.math.BigDecimal" -> String.format("%s.getBigDecimal(%s)", resultSetVar, columnAccessor);
            case "java.sql.Date" -> String.format("%s.getDate(%s)", resultSetVar, columnAccessor);
            case "java.sql.Time" -> String.format("%s.getTime(%s)", resultSetVar, columnAccessor);
            case "java.sql.Timestamp" -> String.format("%s.getTimestamp(%s)", resultSetVar, columnAccessor);
            case "byte[]" -> String.format("%s.getBytes(%s)", resultSetVar, columnAccessor);
            default -> String.format(
                    "%s.getObject(%s, %s.class)",
                    resultSetVar,
                    columnAccessor,
                    getSimpleClassName(typeName)
            );
        };
    }

    protected static String getSimpleClassName(final String qualifiedName){
        final var lastDotIndex = qualifiedName.lastIndexOf(".");
        return lastDotIndex == -1 ?
                qualifiedName :
                qualifiedName.substring(lastDotIndex + 1);
    }

    protected static String toQuotedString(final String value) {
        return "\"" + value + "\"";
    }

}
