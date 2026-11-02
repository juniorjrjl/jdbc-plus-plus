package br.com.jdbcpp.dao.interfaces;

import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.api.method.write.Command;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import static br.com.jdbcpp.api.method.write.CommandType.DELETE;

@DAO
public interface DeleteCategoryDAO {

    @Command(value = "DELETE from categories", commandType = DELETE)
    void deleteFixData() throws SQLException;

    @Command(value = """
            DELETE from categories
            """, returnRowsAffected = true, commandType = DELETE)
    int deleteFixDataRowsAffectedInt() throws SQLException;

    @Command(value = """
            DELETE from categories
            """, returnRowsAffected = true, commandType = DELETE)
    Integer deleteFixDataRowsAffectedInteger() throws SQLException;

    @Command(value = """
            DELETE from categories
            """, returnRowsAffected = true, commandType = DELETE)
    long deleteFixDataRowsAffectedLongPrimitive() throws SQLException;

    @Command(value = """
            DELETE from categories
            """, returnRowsAffected = true, commandType = DELETE)
    Long deleteFixDataRowsAffectedLongClass() throws SQLException;

    @Command(value = "DELETE from categories where id = :id:")
    void deleteById(final Long id) throws SQLException;

    @Command(value = "DELETE from categories where id in :id++:")
    void deleteByIdsList(final List<Long> id) throws SQLException;

    @Command(value = "DELETE from categories where id in :id++:")
    void deleteByIdsSet(final Set<Long> id) throws SQLException;

    @Command(value = "DELETE from categories where id in :id++:")
    void deleteByIdsArray(final Long[] id) throws SQLException;

}
