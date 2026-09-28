package br.com.jdbcpp.dao.abstracts;

import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.api.method.customize.InputMapRange;
import br.com.jdbcpp.api.method.customize.ResultSetMap;
import br.com.jdbcpp.api.method.write.Command;
import br.com.jdbcpp.dto.user.insert.UserInsertPKUUIDDTO;

import javax.sql.DataSource;
import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@DAO
public abstract class InsertUserDAO {

    protected final DataSource dataSource;

    public InsertUserDAO(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Command(value = """
            INSERT INTO users (user_identifier, first_name, last_name, email, birth_date)
            VALUES (:user_identifier:, :firstName:, :lastName:, :email:, :birthDate:)
            """, returnedDataNameOrIndex = "id")
    public abstract UUID insertReturnUUIDPK(final UserInsertPKUUIDDTO dto) throws SQLException;

    @Command(value = """
            INSERT INTO users (user_identifier, first_name, last_name, email, birth_date)
            VALUES (:user_identifier:, :firstName:, :lastName:, :email:, :birthDate:)
            """)
    public abstract void insertSQLServer(final UserInsertPKUUIDDTO dto) throws SQLException;

    @Command(value = """
            INSERT INTO users (user_identifier, first_name, last_name, email, birth_date)
            VALUES (:user_identifier:, :firstName:, :lastName:, :email:, :birthDate:)
            """, returnedDataNameOrIndex = "id")
    @ResultSetMap("toUUID")
    @InputMapRange(value = "toBytes", start = 1, end = 1)
    public abstract UUID insertReturnUUIDPKMySQLAndOracle(final UserInsertPKUUIDDTO dto) throws SQLException;

    protected byte[] toBytes(final PreparedStatement stmt,
                           final UserInsertPKUUIDDTO dto,
                           final int paramIndex) throws SQLException {
        final var bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(dto.userIdentifier().getMostSignificantBits());
        bb.putLong(dto.userIdentifier().getLeastSignificantBits());
        stmt.setBytes(paramIndex, bb.array());
        return bb.array();
    }

    protected UUID toUUID(final ResultSet rs, final int paramIndex) throws SQLException{
        final var bb = ByteBuffer.wrap(rs.getBytes(paramIndex));
        long mostSigBits = bb.getLong();
        long leastSigBits = bb.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }

}
