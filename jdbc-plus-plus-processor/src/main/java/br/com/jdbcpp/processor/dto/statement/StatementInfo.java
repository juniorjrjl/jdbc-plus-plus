package br.com.jdbcpp.processor.dto.statement;

import java.util.List;

public record StatementInfo(
        List<String> sql,
        List<StatementParam> params
) {

    public boolean sqlNotSplit() {
        return sql.size() == 1;
    }

    public String fullSql() {
        return sql.getFirst();
    }

}
