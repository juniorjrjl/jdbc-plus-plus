package br.com.jdbcpp.processor.service.dao.read.select.param;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.stream.Stream;

import static br.com.jdbcpp.api.ResultBuildStrategyType.CONSTRUCTOR;
import static br.com.jdbcpp.api.ResultBuildStrategyType.SETTER;
import static br.com.jdbcpp.api.ResultBuildStrategyType.SIMPLE_RESULT;

public class SelectCollectionMethodGeneratorArgumentProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters,
                                                        final ExtensionContext context) {
        final var returnTypeStr = "com.example.SelectCollectionMethodGeneratorTest.User";
        return Stream.of(
                Arguments.of(returnTypeStr, CONSTRUCTOR, false, false, """
                        public java.util.List<com.example.SelectCollectionMethodGeneratorTest.User> selectUser() throws
                            java.sql.SQLException {
                          final var statement = "SELECT * FROM users";
                          try (final var conn = getConnection();
                          final var stmt = conn.prepareStatement(statement))
                           {
                            var paramIndex = 1;
                            try (final var rs = stmt.executeQuery()) {
                              final java.util.List<com.example.SelectCollectionMethodGeneratorTest.User> result = new java.util.ArrayList<>();
                              while (rs.next()) {
                                final var rsId = rs.getString("id");
                                final var model = new com.example.SelectCollectionMethodGeneratorTest.User(rsId);
                                result.add(model);
                              }
                              return result;
                            }
                          } catch (final java.sql.SQLException e) {
                            throw e;
                          }
                        }
                        """),
                Arguments.of(returnTypeStr, CONSTRUCTOR, false, true, """
                        public java.util.List<com.example.SelectCollectionMethodGeneratorTest.User> selectUser() {
                          final var statement = "SELECT * FROM users";
                          try (final var conn = getConnection();
                          final var stmt = conn.prepareStatement(statement))
                           {
                            var paramIndex = 1;
                            try (final var rs = stmt.executeQuery()) {
                              final java.util.List<com.example.SelectCollectionMethodGeneratorTest.User> result = new java.util.ArrayList<>();
                              while (rs.next()) {
                                final var rsId = rs.getString("id");
                                final var model = new com.example.SelectCollectionMethodGeneratorTest.User(rsId);
                                result.add(model);
                              }
                              return result;
                            }
                          } catch (final java.sql.SQLException e) {
                            throw new java.lang.RuntimeException(e);
                          }
                        }
                        """),
                Arguments.of(returnTypeStr, SETTER, false, false, """
                        public java.util.List<com.example.SelectCollectionMethodGeneratorTest.User> selectUser() throws
                            java.sql.SQLException {
                          final var statement = "SELECT * FROM users";
                          try (final var conn = getConnection();
                          final var stmt = conn.prepareStatement(statement))
                           {
                            var paramIndex = 1;
                            try (final var rs = stmt.executeQuery()) {
                              final java.util.List<com.example.SelectCollectionMethodGeneratorTest.User> result = new java.util.ArrayList<>();
                              while (rs.next()) {
                                final var model = new com.example.SelectCollectionMethodGeneratorTest.User();
                                final var rsId = rs.getString("id");
                                model.setId(rsId);
                                result.add(model);
                              }
                              return result;
                            }
                          } catch (final java.sql.SQLException e) {
                            throw e;
                          }
                        }
                        """),
                Arguments.of("java.lang.String", SIMPLE_RESULT, false, false, """
                        public java.util.List<java.lang.String> selectUser() throws java.sql.SQLException {
                          final var statement = "SELECT * FROM users";
                          try (final var conn = getConnection();
                          final var stmt = conn.prepareStatement(statement))
                           {
                            var paramIndex = 1;
                            try (final var rs = stmt.executeQuery()) {
                              final java.util.List<java.lang.String> result = new java.util.ArrayList<>();
                              while (rs.next()) {
                                final var model = rs.getString(0);
                                result.add(model);
                              }
                              return result;
                            }
                          } catch (final java.sql.SQLException e) {
                            throw e;
                          }
                        }
                        """),
                Arguments.of(returnTypeStr, CONSTRUCTOR, true, false, """
                        public java.util.List<com.example.SelectCollectionMethodGeneratorTest.User> selectUser() throws
                            java.sql.SQLException {
                          final var statement = "SELECT * FROM users";
                          try(final var conn = getConnection();
                          final var stmt = conn.createStatement();
                          final var rs = stmt.executeQuery(statement))
                           {
                            final java.util.List<com.example.SelectCollectionMethodGeneratorTest.User> result = new java.util.ArrayList<>();
                            while (rs.next()) {
                              final var rsId = rs.getString("id");
                              final var model = new com.example.SelectCollectionMethodGeneratorTest.User(rsId);
                              result.add(model);
                            }
                            return result;
                          } catch (final java.sql.SQLException e) {
                            throw e;
                          }
                        }
                        """)
        );
    }
}
