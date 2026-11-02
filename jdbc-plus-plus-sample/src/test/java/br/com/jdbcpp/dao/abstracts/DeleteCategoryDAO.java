package br.com.jdbcpp.dao.abstracts;

import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.api.method.write.Command;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import static br.com.jdbcpp.api.method.write.CommandType.DELETE;

@DAO
public abstract class DeleteCategoryDAO {

    protected final DataSource dataSource;

    public DeleteCategoryDAO(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Command(value = "DELETE from categories", commandType = DELETE)
    public abstract void deleteFixData() throws SQLException;

    @Command(value = """
            DELETE from categories
            """, returnRowsAffected = true, commandType = DELETE)
    public abstract int deleteFixDataRowsAffectedInt() throws SQLException;

    @Command(value = """
            DELETE from categories
            """, returnRowsAffected = true, commandType = DELETE)
    public abstract Integer deleteFixDataRowsAffectedInteger() throws SQLException;

    @Command(value = """
            DELETE from categories
            """, returnRowsAffected = true, commandType = DELETE)
    public abstract long deleteFixDataRowsAffectedLongPrimitive() throws SQLException;

    @Command(value = """
            DELETE from categories
            """, returnRowsAffected = true, commandType = DELETE)
    public abstract Long deleteFixDataRowsAffectedLongClass() throws SQLException;

    @Command(value = "DELETE from categories where id = :id:")
    public abstract void deleteById(final Long id) throws SQLException;

    @Command(value = "DELETE from categories where id in :id++:")
    public abstract void deleteByIdsList(final List<Long> id) throws SQLException;

    @Command(value = "DELETE from categories where id in :id++:")
    public abstract void deleteByIdsSet(final Set<Long> id) throws SQLException;

    @Command(value = "DELETE from categories where id in :id++:")
    public abstract void deleteByIdsArray(final Long[] id) throws SQLException;

}
