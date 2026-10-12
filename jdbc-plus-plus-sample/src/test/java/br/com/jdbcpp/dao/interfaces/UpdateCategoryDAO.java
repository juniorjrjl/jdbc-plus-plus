package br.com.jdbcpp.dao.interfaces;

import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.api.input.InputParam;
import br.com.jdbcpp.api.method.write.Command;
import br.com.jdbcpp.dto.category.CategoryTypeEnum;

import java.sql.SQLException;
import java.time.OffsetDateTime;

import static br.com.jdbcpp.api.method.write.CommandType.UPDATE;

@DAO
public interface UpdateCategoryDAO {

    @Command(value = """
            UPDATE categories
            SET name = 'food changed', updated_at = CURRENT_TIMESTAMP
            """, commandType = UPDATE)
    void updateFixData() throws SQLException;

    @Command(value = """
            UPDATE categories
            SET name = 'mobília changed', updated_at = CURRENT_TIMESTAMP
            """, commandType = UPDATE, returnRowsAffected = true)
    int updateFixDataRowsAffectedInt() throws SQLException;

    @Command(value = """
            UPDATE categories
            SET name = 'cozinha changed', updated_at = CURRENT_TIMESTAMP
            """, commandType = UPDATE, returnRowsAffected = true)
    Integer updateFixDataRowsAffectedInteger() throws SQLException;

    @Command(value = """
            UPDATE categories
            SET name = 'tecnologia changed', updated_at = CURRENT_TIMESTAMP
            """, commandType = UPDATE, returnRowsAffected = true)
    long updateFixDataRowsAffectedLongPrimitive() throws SQLException;

    @Command(value = """
            UPDATE categories
            SET name = 'casa changed', updated_at = CURRENT_TIMESTAMP
            """, commandType = UPDATE, returnRowsAffected = true)
    Long updateFixDataRowsAffectedLongClass() throws SQLException;

    @Command(value = """
            UPDATE categories SET
                   name = :name:,
                   updated_at = :updatedAt:
            WHERE id = :id:
            """, commandType = UPDATE, returnRowsAffected = true)
    long updateFixDataReturnRecord(final String name,
                                   final OffsetDateTime updatedAt,
                                   final long id) throws SQLException;

    @Command(value = "UPDATE categories SET name = :name:, priority = :priority: WHERE id = :id:", commandType = UPDATE)
    void updateEnumToStringAndInt(@InputParam(statementField = "priority", enumMethodValue = "ordinal")
                                  final CategoryTypeEnum categoryOrder,
                                  @InputParam(statementField = "name")
                                  final CategoryTypeEnum categoryName,
                                  final long id) throws SQLException;

    @Command(value = "UPDATE categories SET name = :name: WHERE id = :id:", commandType = UPDATE)
    void updateEnumCustomMethod(@InputParam(statementField = "name", enumMethodValue = "getEnumNameLowerCase")
                                final CategoryTypeEnum category,
                                final long id) throws SQLException;
}
