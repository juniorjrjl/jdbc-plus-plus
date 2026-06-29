package br.com.jdbcpp.processor.util;

import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;

public final class JDBCUtil {

    private  JDBCUtil() {}

    public static String getPrepareStatementSetter(final String name,
                                                   final TypeName type,
                                                   final String convertMethod,
                                                   final boolean isEnum,
                                                   final String prepareStmtVar,
                                                   final String paramIndex) {
        if (isEnum){
            if (convertMethod.contains(name + ".")){
                if (convertMethod.contains("toString")){
                    return String.format(
                            "%s.setString(%s, %s())",
                            prepareStmtVar,
                            paramIndex,
                            convertMethod
                    );
                }
            } else {
                return String.format(
                        "%s(%s, %s, %s)",
                        convertMethod,
                        prepareStmtVar,
                        paramIndex,
                        name
                );
            }
        }

        return switch (type.toString()) {
            case "int" -> String.format("%s.setInt(%s, %s)", prepareStmtVar, paramIndex, name);
            case "long" -> String.format("%s.setLong(%s, %s)", prepareStmtVar, paramIndex, name);
            case "double" -> String.format("%s.setDouble(%s, %s)", prepareStmtVar, paramIndex, name);
            case "float" -> String.format("%s.setFloat(%s, %s)", prepareStmtVar, paramIndex, name);
            case "boolean" -> String.format("%s.setBoolean(%s, %s)", prepareStmtVar, paramIndex, name);
            case "short" -> String.format("%s.setShort(%s, %s)", prepareStmtVar, paramIndex, name);
            case "byte" -> String.format("%s.setByte(%s, %s)", prepareStmtVar, paramIndex, name);
            case "java.lang.String" -> String.format("%s.setString(%s, %s)", prepareStmtVar, paramIndex, name);
            case "java.math.BigDecimal" -> String.format("%s.setBigDecimal(%s, %s)", prepareStmtVar, paramIndex, name);
            case "java.sql.Date" -> String.format("%s.setDate(%s, %s)", prepareStmtVar, paramIndex, name);
            case "java.sql.Time" -> String.format("%s.setTime(%s, %s)", prepareStmtVar, paramIndex, name);
            case "java.sql.Timestamp" -> String.format("%s.setTimestamp(%s, %s)", prepareStmtVar, paramIndex, name);
            case "byte[]" -> String.format("%s.setBytes(%s, %s)", prepareStmtVar, paramIndex, name);
            default -> String.format("%s.setObject(%s, %s)", prepareStmtVar, paramIndex, name);
        };
    }

    public static String getResultSetGetter(final TypeName type,
                                          final String columnAccessor,
                                          final String resultSetVar,
                                          final String varName,
                                          final MethodSpec.Builder builder) {
        final String typeName = type.toString();
        final var rsVarName = "rs"+ varName.substring(0, 1).toUpperCase() + varName.substring(1);
        switch (typeName) {
            case "int", "java.lang.Integer" -> builder.addStatement("final var $L = $L.getInt($L)", rsVarName, resultSetVar, columnAccessor);
            case "long", "java.lang.Long" -> builder.addStatement("final var $L = $L.getLong($L)", rsVarName, resultSetVar, columnAccessor);
            case "double", "java.lang.Double" -> builder.addStatement("final var $L = $L.getDouble($L)", rsVarName, resultSetVar, columnAccessor);
            case "float", "java.lang.Float" -> builder.addStatement("final var $L = $L.getFloat($L)", rsVarName, resultSetVar, columnAccessor);
            case "boolean", "java.lang.Boolean" -> builder.addStatement("final var $L = $L.getBoolean($L)", rsVarName, resultSetVar, columnAccessor);
            case "short", "java.lang.Short" -> builder.addStatement("final var $L = $L.getShort($L)", rsVarName, resultSetVar, columnAccessor);
            case "byte", "java.lang.Byte" -> builder.addStatement("final var $L = $L.getByte($L)", rsVarName, resultSetVar, columnAccessor);
            case "java.lang.String" -> builder.addStatement("final var $L = $L.getString($L)", rsVarName, resultSetVar, columnAccessor);
            case "java.math.BigDecimal" -> builder.addStatement("final var $L = $L.getBigDecimal($L)", rsVarName, resultSetVar, columnAccessor);
            case "java.sql.Date" -> builder.addStatement("final var $L = $L.getDate($L)", rsVarName, resultSetVar, columnAccessor);
            case "java.sql.Time" -> builder.addStatement("final var $L = $L.getTime($L)", rsVarName, resultSetVar, columnAccessor);
            case "java.sql.Timestamp" -> builder.addStatement("final var $L = $L.getTimestamp($L)", rsVarName, resultSetVar, columnAccessor);
            case "byte[]" -> builder.addStatement("final var $L = $L.getBytes($L)", rsVarName, resultSetVar, columnAccessor);
            default -> builder.addStatement("final var $L = $L.getObject($L, $T.class)", rsVarName, resultSetVar, columnAccessor, type);
        }
        return rsVarName;
    }

}
