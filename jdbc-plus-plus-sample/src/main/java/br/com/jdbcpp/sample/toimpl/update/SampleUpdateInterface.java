package br.com.jdbcpp.sample.toimpl.update;

import br.com.jdbcpp.api.Command;
import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.sample.domain.UserClass;
import br.com.jdbcpp.sample.domain.UserRecord;

import java.sql.SQLException;

import static br.com.jdbcpp.api.CommandType.UPDATE;

@DAO
public interface SampleUpdateInterface {

    @Command(value = """
            UPDATE user
            SET
                created_at = now(),
                updated_at = now();
            """, commandType = UPDATE)
    void updateDBDates() throws SQLException;

    @Command(value = """
            UPDATE user
            SET
                created_at = now(),
                updated_at = now();
            """, commandType = UPDATE, returnRowsAffected = true)
    int updateDBRowsAffected() throws SQLException;

    @Command(value = """
            UPDATE user
            SET
                created_at = now(),
                updated_at = now(),
            WHERE id = :id:;
            """, commandType = UPDATE)
    void updateById(final Long id) throws SQLException;

    @Command(value = """
            UPDATE user
            SET
                name = :name:,
            WHERE id = :id:;
            """, commandType = UPDATE)
    void updateRecord(final UserRecord userRecord) throws SQLException;

    @Command(value = """
            UPDATE user
            SET
                name = :name:,
            WHERE id = :id:;
            """, commandType = UPDATE)
    UserClass updateClass(final UserClass userClass) throws SQLException;

}
