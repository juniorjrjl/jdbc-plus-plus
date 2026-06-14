package br.com.jdbcpp.processor.dto.statement;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Objects.nonNull;

public final class StatementInfoFactory {

    private static final Pattern PARAM_PATTERN = Pattern.compile(
            "(?::([a-zA-Z0-9_]+)\\+\\+:)|(?::([a-zA-Z0-9_]+):)"
    );

    private  StatementInfoFactory() {}

    public static StatementInfo create(final String sql) {
        final List<StatementParam> params = new ArrayList<>();
        final List<String> statement = new ArrayList<>();
        final var matcher = PARAM_PATTERN.matcher(sql);
        final var currentBlockBuilder = new StringBuilder();
        var lastEnd = 0;

        while (matcher.find()) {

            currentBlockBuilder.append(sql, lastEnd, matcher.start());
            final var manyParams = matcher.group(1);
            final var paramName = matcher.group(2);

            if (nonNull(manyParams)) {
                params.add(StatementParam.many(manyParams));
                statement.add(currentBlockBuilder.toString());
                currentBlockBuilder.setLength(0);
            } else {
                params.add(StatementParam.simple(paramName));
                currentBlockBuilder.append("?");
            }
            lastEnd = matcher.end();
        }

        currentBlockBuilder.append(sql, lastEnd, sql.length());
        statement.add(currentBlockBuilder.toString());
        return new StatementInfo(statement, params);
    }

}
