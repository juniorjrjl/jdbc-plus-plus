package br.com.jdbcpp.processor.service.dao.read.select.param;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.stream.Stream;

import static br.com.jdbcpp.api.ResultBuildStrategyType.CONSTRUCTOR;
import static br.com.jdbcpp.api.ResultBuildStrategyType.SETTER;
import static br.com.jdbcpp.api.ResultBuildStrategyType.SIMPLE_RESULT;

public class SelectSingleMethodGeneratorArgumentProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters,
                                                        final ExtensionContext context) {
        final var returnType = "com.example.SelectSingleMethodGeneratorTest.User";
        return Stream.of(
                Arguments.of(returnType, CONSTRUCTOR, false, false, """
                        public com.example.SelectSingleMethodGeneratorTest.User selectUser() throws java.sql.SQLException {
                          final var statement = "SELECT * FROM users";
                          try (final var conn = getConnection();
                          final var stmt = conn.prepareStatement(statement))
                           {
                            var paramIndex = 1;
                            try (final var rs = stmt.executeQuery()) {
                              if (rs.next()) {
                                final var rsId = rs.getString("id");
                                final var model = new com.example.SelectSingleMethodGeneratorTest.User(rsId);
                                return model;
                              } else {
                                return null;
                              }
                            }
                          } catch (final java.sql.SQLException e) {
                            throw e;
                          }
                        }
                        """),
                Arguments.of(returnType, CONSTRUCTOR, false, true, """
                        public com.example.SelectSingleMethodGeneratorTest.User selectUser() {
                          final var statement = "SELECT * FROM users";
                          try (final var conn = getConnection();
                          final var stmt = conn.prepareStatement(statement))
                           {
                            var paramIndex = 1;
                            try (final var rs = stmt.executeQuery()) {
                              if (rs.next()) {
                                final var rsId = rs.getString("id");
                                final var model = new com.example.SelectSingleMethodGeneratorTest.User(rsId);
                                return model;
                              } else {
                                return null;
                              }
                            }
                          } catch (final java.sql.SQLException e) {
                            throw new java.lang.RuntimeException(e);
                          }
                        }
                        """),
                Arguments.of(returnType, SETTER, false, false, """
                        public com.example.SelectSingleMethodGeneratorTest.User selectUser() throws java.sql.SQLException {
                          final var statement = "SELECT * FROM users";
                          try (final var conn = getConnection();
                          final var stmt = conn.prepareStatement(statement))
                           {
                            var paramIndex = 1;
                            try (final var rs = stmt.executeQuery()) {
                              if (rs.next()) {
                                final var model = new com.example.SelectSingleMethodGeneratorTest.User();
                                final var rsId = rs.getString("id");
                                model.setId(rsId);
                                return model;
                              } else {
                                return null;
                              }
                            }
                          } catch (final java.sql.SQLException e) {
                            throw e;
                          }
                        }
                        """),
                Arguments.of("java.lang.String", SIMPLE_RESULT, false, false, """
                        public java.lang.String selectUser() throws java.sql.SQLException {
                          final var statement = "SELECT * FROM users";
                          try (final var conn = getConnection();
                          final var stmt = conn.prepareStatement(statement))
                           {
                            var paramIndex = 1;
                            try (final var rs = stmt.executeQuery()) {
                              if (rs.next()) {
                                final var model = new java.lang.String();
                                final var rsUnnamed = rs.getString(-1);
                                return model;
                              } else {
                                return null;
                              }
                            }
                          } catch (final java.sql.SQLException e) {
                            throw e;
                          }
                        }
                        """),
                Arguments.of(returnType, CONSTRUCTOR, true, false, """
                        public com.example.SelectSingleMethodGeneratorTest.User selectUser() throws java.sql.SQLException {
                          final var statement = "SELECT * FROM users";
                          try(final var conn = getConnection();
                          final var stmt = conn.createStatement();
                          final var rs = stmt.executeQuery(statement))
                           {
                            if (rs.next()) {
                              final var rsId = rs.getString("id");
                              final var model = new com.example.SelectSingleMethodGeneratorTest.User(rsId);
                              return model;
                            } else {
                              return null;
                            }
                          } catch (final java.sql.SQLException e) {
                            throw e;
                          }
                        }
                        """)
        );
    }
}
