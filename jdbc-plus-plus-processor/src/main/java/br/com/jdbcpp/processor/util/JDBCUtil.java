package br.com.jdbcpp.processor.util;

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
            default -> String.format("%s.getObject(%s, %s.class)", resultSetVar, columnAccessor, TypeUtil.getSimpleClassName(typeName)
            );
        };
    }

}
