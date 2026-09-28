package br.com.jdbcpp.dao.abstracts;

import br.com.jdbcpp.api.DAO;
import br.com.jdbcpp.api.input.InputParam;
import br.com.jdbcpp.api.method.write.Command;
import br.com.jdbcpp.dto.category.CategoryTypeEnum;
import br.com.jdbcpp.dto.category.insert.CategoryClassDTO;
import br.com.jdbcpp.dto.category.insert.CategoryClassDTOWithIgnoreProp;
import br.com.jdbcpp.dto.category.insert.CategoryDTO;
import br.com.jdbcpp.exception.CustomSQLException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.OffsetDateTime;

@DAO
public abstract class InsertCategoryDAO {

    protected final DataSource dataSource;

    public InsertCategoryDAO(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Command(value = """
            INSERT INTO categories (name, created_at, updated_at)
            VALUES ('food', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """)
    public abstract void insertFixData() throws SQLException;

    @Command(value = """
            INSERT INTO categories (name, created_at, updated_at)
            VALUES ('mobília', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """, returnRowsAffected = true)
    public abstract int insertFixDataRowsAffectedInt() throws SQLException;

    @Command(value = """
            INSERT INTO categories (name, created_at, updated_at)
            VALUES ('cozinha', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """, returnRowsAffected = true)
    public abstract Integer insertFixDataRowsAffectedInteger() throws SQLException;

    @Command(value = """
            INSERT INTO categories (name, created_at, updated_at)
            VALUES ('tecnologia', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """, returnRowsAffected = true)
    public abstract long insertFixDataRowsAffectedLongPrimitive() throws SQLException;

    @Command(value = """
            INSERT INTO categories (name, created_at, updated_at)
            VALUES ('casa', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """, returnRowsAffected = true)
    public abstract Long insertFixDataRowsAffectedLongClass() throws SQLException;

    @Command(value = """
            INSERT INTO categories (name, created_at, updated_at)
            VALUES (:name:, :createdAt:, :updatedAt:)
            """, returnedDataNameOrIndex = "id")
    public abstract long insertFixDataReturnRecord(final String name,
                                   final OffsetDateTime createdAt,
                                   final OffsetDateTime updatedAt) throws SQLException;

    @Command(value = "INSERT INTO categories (name, priority) VALUES (:name:, :priority:)")
    public abstract void insertInsertEnumToStringAndInt(@InputParam(statementField = "priority", enumMethodValue = "ordinal")
                                        final CategoryTypeEnum categoryOrder,
                                        @InputParam(statementField = "name")
                                        final CategoryTypeEnum categoryName) throws SQLException;

    @Command(value = "INSERT INTO categories (name) VALUES (:name:)")
    public abstract void insertInsertEnumCustomMethod(@InputParam(statementField = "name", enumMethodValue = "getEnumNameLowerCase") final CategoryTypeEnum category) throws SQLException;


    @Command(value = """
            INSERT INTO categories (name, created_at, updated_at)
            VALUES (:name:, :createdAt:, :updatedAt:)
            """, returnedDataNameOrIndex = "id")
    public abstract int insertFixDataReturnIdByColumnName(final CategoryDTO dto) throws SQLException;

    @Command(value = """
            INSERT INTO categories (name, created_at, updated_at)
            VALUES (:name:, :createdAt:, :updatedAt:)
            """, returnedDataNameOrIndex = "1")
    public abstract Integer insertFixDataReturnIdByColumnIndex(final CategoryClassDTO dto) throws SQLException;

    @Command(value = """
            INSERT INTO categories (name, created_at, updated_at)
            VALUES (:name:, :createdAt:, :updatedAt:)
            """)
    public abstract void insertFixDataClassGetterPG(final CategoryClassDTO dto) throws SQLException;

    @Command(value = "INSERT INTO categories (name) VALUES (:category_name:)", packException = CustomSQLException.class)
    public abstract void insertMappedFields(final CategoryClassDTOWithIgnoreProp dto);

}
