package br.com.jdbcpp.dao.interfaces;

import br.com.jdbcpp.api.method.write.Command;
import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.api.input.InputParam;
import br.com.jdbcpp.dto.category.CategoryTypeEnum;
import br.com.jdbcpp.dto.category.insert.CategoryClassDTO;
import br.com.jdbcpp.dto.category.insert.CategoryClassDTOWithIgnoreProp;
import br.com.jdbcpp.dto.category.insert.CategoryDTO;
import br.com.jdbcpp.exception.CustomSQLException;

import java.sql.SQLException;
import java.time.OffsetDateTime;

@DAO
public interface InsertCategoryDAO {

    @Command(value = """
            INSERT INTO categories (name, created_at, updated_at)
            VALUES ('food', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """)
    void insertFixData() throws SQLException;

    @Command(value = """
            INSERT INTO categories (name, created_at, updated_at)
            VALUES ('mobília', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """, returnRowsAffected = true)
    int insertFixDataRowsAffectedInt() throws SQLException;

    @Command(value = """
            INSERT INTO categories (name, created_at, updated_at)
            VALUES ('cozinha', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """, returnRowsAffected = true)
    Integer insertFixDataRowsAffectedInteger() throws SQLException;

    @Command(value = """
            INSERT INTO categories (name, created_at, updated_at)
            VALUES ('tecnologia', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """, returnRowsAffected = true)
    long insertFixDataRowsAffectedLongPrimitive() throws SQLException;

    @Command(value = """
            INSERT INTO categories (name, created_at, updated_at)
            VALUES ('casa', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """, returnRowsAffected = true)
    Long insertFixDataRowsAffectedLongClass() throws SQLException;

    @Command(value = """
            INSERT INTO categories (name, created_at, updated_at)
            VALUES (:name:, :createdAt:, :updatedAt:)
            """, returnedDataNameOrIndex = "id")
    long insertFixDataReturnRecord(final String name,
                                   final OffsetDateTime createdAt,
                                   final OffsetDateTime updatedAt) throws SQLException;

    @Command(value = "INSERT INTO categories (name, priority) VALUES (:name:, :priority:)")
    void insertInsertEnumToStringAndInt(@InputParam(statementField = "priority", enumMethodValue = "ordinal")
                                        final CategoryTypeEnum categoryOrder,
                                        @InputParam(statementField = "name")
                                        final CategoryTypeEnum categoryName) throws SQLException;

    @Command(value = "INSERT INTO categories (name) VALUES (:name:)")
    void insertInsertEnumCustomMethod(@InputParam(statementField = "name", enumMethodValue = "getEnumNameLowerCase") final CategoryTypeEnum category) throws SQLException;


    @Command(value = """
            INSERT INTO categories (name, created_at, updated_at)
            VALUES (:name:, :createdAt:, :updatedAt:)
            """, returnedDataNameOrIndex = "id")
    int insertFixDataReturnIdByColumnName(final CategoryDTO dto) throws SQLException;

    @Command(value = """
            INSERT INTO categories (name, created_at, updated_at)
            VALUES (:name:, :createdAt:, :updatedAt:)
            """, returnedDataNameOrIndex = "1")
    Integer insertFixDataReturnIdByColumnIndex(final CategoryClassDTO dto) throws SQLException;

    @Command(value = """
            INSERT INTO categories (name, created_at, updated_at)
            VALUES (:name:, :createdAt:, :updatedAt:)
            """)
    void insertFixDataClassGetterPG(final CategoryClassDTO dto) throws SQLException;

    @Command(value = "INSERT INTO categories (name) VALUES (:category_name:)", packException = CustomSQLException.class)
    void insertMappedFields(final CategoryClassDTOWithIgnoreProp dto);

}
