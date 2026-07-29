package br.com.jdbcpp.processor.service.statement;

import br.com.jdbcpp.processor.dto.statement.StatementParam;
import br.com.jdbcpp.processor.dto.statement.StatementParamType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class StatementInfoFactoryTest {

    private record TestCase(
            String description,
            String inputSql,
            List<String> expectedSqlBlocks,
            List<StatementParam> expectedParams
    ) {}

    @ParameterizedTest
    @MethodSource
    void shouldCreateStatementInfo(final TestCase testCase) {
        final var result = StatementInfoFactory.create(testCase.inputSql());

        assertThat(result.sql()).isEqualTo(testCase.expectedSqlBlocks());
        assertThat(result.params()).isEqualTo(testCase.expectedParams());
    }

    private static Stream<Arguments> shouldCreateStatementInfo() {
        return Stream.of(
                Arguments.of(new TestCase(
                        "SQL without params",
                        "SELECT * FROM users",
                        List.of("SELECT * FROM users"),
                        List.of()
                )),
                Arguments.of(new TestCase(
                        "Single simple param",
                        "SELECT * FROM users WHERE id = :id:",
                        List.of("SELECT * FROM users WHERE id = ?"),
                        List.of(new StatementParam("id", StatementParamType.SIMPLE))
                )),
                Arguments.of(new TestCase(
                        "Multiple simple params",
                        "SELECT * FROM users WHERE id = :id: AND name = :name:",
                        List.of("SELECT * FROM users WHERE id = ? AND name = ?"),
                        List.of(
                                new StatementParam("id", StatementParamType.SIMPLE),
                                new StatementParam("name", StatementParamType.SIMPLE)
                        )
                )),
                Arguments.of(new TestCase(
                        "Single many param",
                        "SELECT * FROM users WHERE id IN (:ids++:)",
                        List.of("SELECT * FROM users WHERE id IN (", ")"),
                        List.of(new StatementParam("ids", StatementParamType.MANY))
                )),
                Arguments.of(new TestCase(
                        "Multiple many params",
                        "SELECT * FROM users WHERE id IN (:ids++:) AND status IN (:statuses++:)",
                        List.of("SELECT * FROM users WHERE id IN (", ") AND status IN (", ")"),
                        List.of(
                                new StatementParam("ids", StatementParamType.MANY),
                                new StatementParam("statuses", StatementParamType.MANY)
                        )
                )),
                Arguments.of(new TestCase(
                        "Mixed simple and many params",
                        "SELECT * FROM users WHERE id = :id: AND status IN (:statuses++:) AND name = :name:",
                        List.of("SELECT * FROM users WHERE id = ? AND status IN (", ") AND name = ?"),
                        List.of(
                                new StatementParam("id", StatementParamType.SIMPLE),
                                new StatementParam("statuses", StatementParamType.MANY),
                                new StatementParam("name", StatementParamType.SIMPLE)
                        )
                )),
                Arguments.of(new TestCase(
                        "Param at start",
                        ":id: = value",
                        List.of("? = value"),
                        List.of(new StatementParam("id", StatementParamType.SIMPLE))
                )),
                Arguments.of(new TestCase(
                        "Param at end",
                        "value = :id:",
                        List.of("value = ?"),
                        List.of(new StatementParam("id", StatementParamType.SIMPLE))
                )),
                Arguments.of(new TestCase(
                        "Many param at start",
                        ":ids++: IN (1, 2, 3)",
                        List.of("", " IN (1, 2, 3)"),
                        List.of(new StatementParam("ids", StatementParamType.MANY))
                )),
                Arguments.of(new TestCase(
                        "Many param at end",
                        "IN (1, 2, 3) :ids++:",
                        List.of("IN (1, 2, 3) ", ""),
                        List.of(new StatementParam("ids", StatementParamType.MANY))
                )),
                Arguments.of(new TestCase(
                        "Underscore in param name",
                        "SELECT * FROM users WHERE user_id = :user_id:",
                        List.of("SELECT * FROM users WHERE user_id = ?"),
                        List.of(new StatementParam("user_id", StatementParamType.SIMPLE))
                )),
                Arguments.of(new TestCase(
                        "Numbers in param name",
                        "SELECT * FROM users WHERE id = :id123:",
                        List.of("SELECT * FROM users WHERE id = ?"),
                        List.of(new StatementParam("id123", StatementParamType.SIMPLE))
                )),
                Arguments.of(new TestCase(
                        "INSERT statement",
                        "INSERT INTO users (name, age) VALUES (:name:, :age:)",
                        List.of("INSERT INTO users (name, age) VALUES (?, ?)"),
                        List.of(
                                new StatementParam("name", StatementParamType.SIMPLE),
                                new StatementParam("age", StatementParamType.SIMPLE)
                        )
                )),
                Arguments.of(new TestCase(
                        "UPDATE statement",
                        "UPDATE users SET name = :name: WHERE id = :id:",
                        List.of("UPDATE users SET name = ? WHERE id = ?"),
                        List.of(
                                new StatementParam("name", StatementParamType.SIMPLE),
                                new StatementParam("id", StatementParamType.SIMPLE)
                        )
                )),
                Arguments.of(new TestCase(
                        "DELETE statement",
                        "DELETE FROM users WHERE id IN (:ids++:)",
                        List.of("DELETE FROM users WHERE id IN (", ")"),
                        List.of(new StatementParam("ids", StatementParamType.MANY))
                ))
        );
    }

}