package br.com.jdbcpp.processor.service.dao.read.select.param;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.stream.Stream;

import static br.com.jdbcpp.api.ResultBuildStrategyType.CONSTRUCTOR;
import static br.com.jdbcpp.api.ResultBuildStrategyType.SETTER;
import static br.com.jdbcpp.api.ResultBuildStrategyType.SIMPLE_RESULT;

public class SelectOptionalMethodGeneratorArgumentProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters,
                                                        final ExtensionContext context) {
        final var returnTypeStr = "com.example.SelectOptionalMethodGeneratorTest.User";
        return Stream.of(
                Arguments.of(returnTypeStr, CONSTRUCTOR, false, false, """
                        public java.util.Optional<T> selectUser() throws java.sql.SQLException {
                          final var statement = "SELECT * FROM users";
                          try (final var conn = getConnection();
                          final var stmt = conn.prepareStatement(statement))
                           {
                            var paramIndex = 1;
                            try (final var rs = stmt.executeQuery()) {
                              if (rs.next()) {
                                final var rsId = rs.getString("id");
                                final var model = new com.example.SelectOptionalMethodGeneratorTest.User(rsId);
                                return java.util.Optional.of(model);
                              } else {
                                return java.util.Optional.empty();
                              }
                            }
                          } catch (final java.sql.SQLException e) {
                            throw e;
                          }
                        }
                        """),
                Arguments.of(returnTypeStr, CONSTRUCTOR, false, true, """
                        public java.util.Optional<T> selectUser() {
                          final var statement = "SELECT * FROM users";
                          try (final var conn = getConnection();
                          final var stmt = conn.prepareStatement(statement))
                           {
                            var paramIndex = 1;
                            try (final var rs = stmt.executeQuery()) {
                              if (rs.next()) {
                                final var rsId = rs.getString("id");
                                final var model = new com.example.SelectOptionalMethodGeneratorTest.User(rsId);
                                return java.util.Optional.of(model);
                              } else {
                                return java.util.Optional.empty();
                              }
                            }
                          } catch (final java.sql.SQLException e) {
                            throw new java.lang.RuntimeException(e);
                          }
                        }
                        """),
                Arguments.of(returnTypeStr, SETTER, false, false, """
                        public java.util.Optional<T> selectUser() throws java.sql.SQLException {
                          final var statement = "SELECT * FROM users";
                          try (final var conn = getConnection();
                          final var stmt = conn.prepareStatement(statement))
                           {
                            var paramIndex = 1;
                            try (final var rs = stmt.executeQuery()) {
                              if (rs.next()) {
                                final var model = new com.example.SelectOptionalMethodGeneratorTest.User();
                                final var rsId = rs.getString("id");
                                model.setId(rsId);
                                return java.util.Optional.of(model);
                              } else {
                                return java.util.Optional.empty();
                              }
                            }
                          } catch (final java.sql.SQLException e) {
                            throw e;
                          }
                        }
                        """),
                Arguments.of("java.lang.String", SIMPLE_RESULT, false, false, """
                        public java.util.Optional<T> selectUser() throws java.sql.SQLException {
                          final var statement = "SELECT * FROM users";
                          try (final var conn = getConnection();
                          final var stmt = conn.prepareStatement(statement))
                           {
                            var paramIndex = 1;
                            try (final var rs = stmt.executeQuery()) {
                              if (rs.next()) {
                                final var model = rs.getString(0);
                                return java.util.Optional.of(model);
                              } else {
                                return java.util.Optional.empty();
                              }
                            }
                          } catch (final java.sql.SQLException e) {
                            throw e;
                          }
                        }
                        """),
                Arguments.of(returnTypeStr, CONSTRUCTOR, true, false, """
                        public java.util.Optional<T> selectUser() throws java.sql.SQLException {
                          final var statement = "SELECT * FROM users";
                          try(final var conn = getConnection();
                          final var stmt = conn.createStatement();
                          final var rs = stmt.executeQuery(statement))
                           {
                            if (rs.next()) {
                              final var rsId = rs.getString("id");
                              final var model = new com.example.SelectOptionalMethodGeneratorTest.User(rsId);
                              return java.util.Optional.of(model);
                            } else {
                              return java.util.Optional.empty();
                            }
                          } catch (final java.sql.SQLException e) {
                            throw e;
                          }
                        }
                        """)
        );
    }
}
