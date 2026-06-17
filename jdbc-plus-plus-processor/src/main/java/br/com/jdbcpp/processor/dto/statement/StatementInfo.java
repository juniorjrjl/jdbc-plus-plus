package br.com.jdbcpp.processor.dto.statement;

import java.util.List;

public record StatementInfo(
        List<String> sql,
        List<StatementParam> params
) {

    public StatementInfo {
        sql = List.copyOf(sql);
        params = List.copyOf(params);
    }

    public boolean sqlNotSplit() {
        return sql.size() == 1;
    }

    public String getNoSplitFullSQL() {
        return sql.getFirst();
    }

    public boolean sqlWithoutParams() {
        return params.isEmpty();
    }

}
