package br.com.jdbcpp.processor.service.dao.write.insert.param;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.stream.Stream;

public class InsertMethodGeneratorArgumentProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters,
                                                        final ExtensionContext context) {
        return Stream.of(
                Arguments.of("java.lang.Void", false, false, """
                        public java.lang.Void insertUser() throws java.sql.SQLException {
                          final var statement = "INSERT INTO users";
                          try(final var conn = getConnection();
                          final var stmt = conn.createStatement())
                           {
                            stmt.executeUpdate(statement);
                          } catch (final java.sql.SQLException e) {
                            throw e;
                          }
                        }
                        """),
                Arguments.of("java.lang.Integer", true, false, """
                        public java.lang.Integer insertUser() throws java.sql.SQLException {
                          final var statement = "INSERT INTO users";
                          try(final var conn = getConnection();
                          final var stmt = conn.createStatement())
                           {
                            return stmt.executeUpdate(statement);
                          } catch (final java.sql.SQLException e) {
                            throw e;
                          }
                        }
                        """),
                Arguments.of("java.lang.Long", true, false, """
                        public java.lang.Long insertUser() throws java.sql.SQLException {
                          final var statement = "INSERT INTO users";
                          try(final var conn = getConnection();
                          final var stmt = conn.createStatement())
                           {
                            return java.lang.Long.valueOf(stmt.executeUpdate(statement));
                          } catch (final java.sql.SQLException e) {
                            throw e;
                          }
                        }
                        """),
                Arguments.of("java.lang.Void", false, true, """
                        public java.lang.Void insertUser() {
                          final var statement = "INSERT INTO users";
                          try(final var conn = getConnection();
                          final var stmt = conn.createStatement())
                           {
                            stmt.executeUpdate(statement);
                          } catch (final java.sql.SQLException e) {
                            throw new java.lang.RuntimeException(e);
                          }
                        }
                        """)
        );
    }
}
