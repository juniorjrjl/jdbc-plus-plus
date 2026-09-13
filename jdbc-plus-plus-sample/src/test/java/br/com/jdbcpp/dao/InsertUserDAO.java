package br.com.jdbcpp.dao;

import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.api.method.customize.InputMapRange;
import br.com.jdbcpp.api.method.customize.ResultSetMap;
import br.com.jdbcpp.api.method.write.Command;
import br.com.jdbcpp.dto.user.insert.UserInsertPKUUIDDTO;

import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@DAO
public interface InsertUserDAO {

    @Command(value = """
            INSERT INTO users (user_identifier, first_name, last_name, email, birth_date)
            VALUES (:user_identifier:, :firstName:, :lastName:, :email:, :birthDate:)
            """, returnedDataNameOrIndex = "id")
    UUID insertReturnUUIDPK(final UserInsertPKUUIDDTO dto) throws SQLException;

    @Command(value = """
            INSERT INTO users (user_identifier, first_name, last_name, email, birth_date)
            VALUES (:user_identifier:, :firstName:, :lastName:, :email:, :birthDate:)
            """)
    void insertSQLServer(final UserInsertPKUUIDDTO dto) throws SQLException;

    @Command(value = """
            INSERT INTO users (user_identifier, first_name, last_name, email, birth_date)
            VALUES (:user_identifier:, :firstName:, :lastName:, :email:, :birthDate:)
            """, returnedDataNameOrIndex = "id")
    @ResultSetMap("toUUID")
    @InputMapRange(value = "toBytes", start = 1, end = 1)
    UUID insertReturnUUIDPKMySQLAndOracle(final UserInsertPKUUIDDTO dto) throws SQLException;

    default byte[] toBytes(final PreparedStatement stmt, final UserInsertPKUUIDDTO dto ) throws SQLException {
        final var bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(dto.userIdentifier().getMostSignificantBits());
        bb.putLong(dto.userIdentifier().getLeastSignificantBits());
        stmt.setBytes(1, bb.array());
        return bb.array();
    }

    default UUID toUUID(final ResultSet rs) throws SQLException{
        final var bb = ByteBuffer.wrap(rs.getBytes(1));
        long mostSigBits = bb.getLong();
        long leastSigBits = bb.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }

}
