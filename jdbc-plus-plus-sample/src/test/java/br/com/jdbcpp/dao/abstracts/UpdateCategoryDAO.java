package br.com.jdbcpp.dao.abstracts;

import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.api.input.InputParam;
import br.com.jdbcpp.api.method.write.Command;
import br.com.jdbcpp.dto.category.CategoryTypeEnum;
import br.com.jdbcpp.dto.category.update.CategoryUpdateDTO;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import static br.com.jdbcpp.api.method.write.CommandType.UPDATE;

@DAO
public abstract class UpdateCategoryDAO {

    protected final DataSource dataSource;

    public UpdateCategoryDAO(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Command(value = """
            UPDATE categories
            SET name = 'food changed', updated_at = CURRENT_TIMESTAMP
            """, commandType = UPDATE)
    public abstract void updateFixData() throws SQLException;

    @Command(value = """
            UPDATE categories
            SET name = 'mobília changed', updated_at = CURRENT_TIMESTAMP
            """, commandType = UPDATE, returnRowsAffected = true)
    public abstract int updateFixDataRowsAffectedInt() throws SQLException;

    @Command(value = """
            UPDATE categories
            SET name = 'cozinha changed', updated_at = CURRENT_TIMESTAMP
            """, commandType = UPDATE, returnRowsAffected = true)
    public abstract Integer updateFixDataRowsAffectedInteger() throws SQLException;

    @Command(value = """
            UPDATE categories
            SET name = 'tecnologia changed', updated_at = CURRENT_TIMESTAMP
            """, commandType = UPDATE, returnRowsAffected = true)
    public abstract long updateFixDataRowsAffectedLongPrimitive() throws SQLException;

    @Command(value = """
            UPDATE categories
            SET name = 'casa changed', updated_at = CURRENT_TIMESTAMP
            """, commandType = UPDATE, returnRowsAffected = true)
    public abstract Long updateFixDataRowsAffectedLongClass() throws SQLException;

    @Command(value = """
            UPDATE categories SET
                   name = :name:,
                   updated_at = :updatedAt:
            WHERE id = :id:
            """, commandType = UPDATE, returnRowsAffected = true)
    public abstract long updateFixDataReturnRecord(final String name,
                                   final OffsetDateTime updatedAt,
                                   final long id) throws SQLException;

    @Command(value = "UPDATE categories SET name = :name:, priority = :priority: WHERE id = :id:", commandType = UPDATE)
    public abstract void updateEnumToStringAndInt(@InputParam(statementField = "priority", enumMethodValue = "ordinal")
                                  final CategoryTypeEnum categoryOrder,
                                  @InputParam(statementField = "name")
                                  final CategoryTypeEnum categoryName,
                                  final long id) throws SQLException;

    @Command(value = "UPDATE categories SET name = :name: WHERE id = :id:", commandType = UPDATE)
    public abstract void updateEnumCustomMethod(@InputParam(statementField = "name", enumMethodValue = "getEnumNameLowerCase")
                                final CategoryTypeEnum category,
                                final long id) throws SQLException;

    @Command(value = """
            UPDATE categories SET
                   name = :name:,
                   created_at = :createdAt:,
                   updated_at = :updatedAt:
             WHERE id = :id:
            """, commandType = UPDATE)
    public abstract void updateDTO(final CategoryUpdateDTO dto) throws SQLException;

}
