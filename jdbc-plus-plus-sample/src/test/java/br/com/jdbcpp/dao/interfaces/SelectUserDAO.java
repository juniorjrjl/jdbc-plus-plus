package br.com.jdbcpp.dao.interfaces;

import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.api.method.customize.ColumnMapRange;
import br.com.jdbcpp.api.method.customize.InputMapRange;
import br.com.jdbcpp.api.method.read.Query;
import br.com.jdbcpp.api.method.read.ResultBuildStrategy;
import br.com.jdbcpp.dto.user.select.UserClassConstructor;

import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static br.com.jdbcpp.api.method.read.ResultBuildStrategyType.CONSTRUCTOR;

@DAO
public interface SelectUserDAO {

    @Query(value = """
            SELECT id,
                   user_identifier,
                   first_name,
                   last_name,
                   email,
                   birth_date,
                   created_at,
                   updated_at
              FROM users
             WHERE id = :id:
            """)
    @ResultBuildStrategy(CONSTRUCTOR)
    @InputMapRange(value = "toBytes", start = 1, end = 1)
    @ColumnMapRange(value = "toUUID", start = 1, end = 1)
    @ColumnMapRange(value = "toUUID", start = 2, end = 2)
    Optional<UserClassConstructor> selectOptionalByMappedId(final UUID id) throws SQLException;

    @Query(value = """
            SELECT id,
                   user_identifier,
                   first_name,
                   last_name,
                   email,
                   birth_date,
                   created_at,
                   updated_at
              FROM users
             WHERE id = :id:
            """)
    @ResultBuildStrategy(CONSTRUCTOR)
    Optional<UserClassConstructor> selectOptionalById(final UUID id) throws SQLException;

    @Query(value = """
            SELECT id,
                   user_identifier,
                   first_name,
                   last_name,
                   email,
                   birth_date,
                   created_at,
                   updated_at
              FROM users
            """)
    @ResultBuildStrategy(CONSTRUCTOR)
    List<UserClassConstructor> findAll() throws SQLException;

    default UUID toUUID(final ResultSet rs, final int columnIndex) throws SQLException{
        final var bb = ByteBuffer.wrap(rs.getBytes(columnIndex));
        long mostSigBits = bb.getLong();
        long leastSigBits = bb.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    default byte[] toBytes(final PreparedStatement stmt,
                           final UUID id,
                           final int paramIndex) throws SQLException {
        final var bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(id.getMostSignificantBits());
        bb.putLong(id.getLeastSignificantBits());
        stmt.setBytes(paramIndex, bb.array());
        return bb.array();
    }

}
