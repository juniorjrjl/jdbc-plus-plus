package br.com.jdbcpp.sample.toimpl.insert;

import br.com.jdbcpp.api.Command;
import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.sample.domain.UserClass;
import br.com.jdbcpp.sample.domain.UserRecord;

import java.sql.SQLException;

@DAO
public interface SampleInsertInterface {

    @Command(value = """
            INSERT INTO users (id, name, document)
            VALUES (1, 'Bob', '123456789');
            """)
    void insertNoParams() throws SQLException;

    @Command(value = """
            INSERT INTO users (id, name, document)
            VALUES (1, 'Bob', '123456789');
            """, returnRowsAffected = true)
    int insertRowsAffected() throws SQLException;

    @Command(value = """
            INSERT INTO users (id, name, document)
            VALUES (:id:, :name:, :document:);
            """)
    void insertParams(final Long id, final String name, final String document) throws SQLException;

    @Command(value = """
            INSERT INTO users (id, name)
            VALUES (:id:, :name:);
            """)
    void insertRecord(final UserRecord userRecord) throws SQLException;

    @Command(value = """
            INSERT INTO users (id, name)
            VALUES (:id:, :name:);
            """)
    UserClass insertClass(final UserClass userClass) throws SQLException;


}
