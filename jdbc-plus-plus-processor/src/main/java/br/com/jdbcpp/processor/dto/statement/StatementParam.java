package br.com.jdbcpp.processor.dto.statement;

public record StatementParam(
        String name,
        StatementParamType type
) {

    public static StatementParam simple(final String name) {
        return new StatementParam(name, StatementParamType.SIMPLE);
    }

    public static StatementParam many(final String name) {
        return new StatementParam(name, StatementParamType.MANY);
    }

}
