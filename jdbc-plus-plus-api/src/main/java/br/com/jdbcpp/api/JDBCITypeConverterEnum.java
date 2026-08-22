package br.com.jdbcpp.api;

import org.jspecify.annotations.Nullable;

public enum JDBCITypeConverterEnum {


    NONE(null),
    INT("int"),
    LONG("long"),
    DOUBLE("double"),
    FLOAT("float"),
    BOOLEAN("boolean"),
    SHORT("short"),
    BYTE("byte"),
    STRING("java.lang.String"),
    BIG_DECIMAL("java.math.BigDecimal"),
    DATE("java.sql.Date"),
    TIME("java.sql.Time"),
    TIMESTAMP("java.sql.Timestamp"),
    BYTES("byte[]");

    @Nullable
    private final String type;

    JDBCITypeConverterEnum(@Nullable final String type) {
        this.type = type;
    }

    @Nullable
    public String getType() {
        return type;
    }

}
