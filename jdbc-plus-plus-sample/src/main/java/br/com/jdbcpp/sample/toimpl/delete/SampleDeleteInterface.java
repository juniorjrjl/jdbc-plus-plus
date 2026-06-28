package br.com.jdbcpp.sample.toimpl.delete;

import br.com.jdbcpp.api.Command;
import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.sample.domain.UserClass;
import br.com.jdbcpp.sample.domain.UserRecord;

import java.sql.SQLException;

import static br.com.jdbcpp.api.CommandType.DELETE;

@DAO
public interface SampleDeleteInterface {

    @Command(value = "DELETE FROM user WHERE id = :id:;", commandType = DELETE)
    void deleteById(final long id) throws SQLException;

    @Command(value = "DELETE FROM user;", commandType = DELETE)
    void deleteALL() throws SQLException;

    @Command(value = "DELETE FROM user;", commandType = DELETE, returnRowsAffected = true)
    long deleteWithRowsAffected() throws SQLException;

    @Command(value = "DELETE FROM user where id = :id: and name = :name:;", commandType = DELETE)
    void deleteRecord(final UserRecord userRecord) throws SQLException;

    @Command(value = "DELETE FROM user where id = :id: and name = :name:;", commandType = DELETE)
    void deleteClass(final UserClass userClass) throws SQLException;

    @Command(value = "DELETE FROM user where id in (:ids++:);", commandType = DELETE)
    void deleteMany(final int[] ids) throws SQLException;

}
