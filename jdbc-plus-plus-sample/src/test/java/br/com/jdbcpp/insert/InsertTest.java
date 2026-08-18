package br.com.jdbcpp.insert;

import br.com.jdbcpp.dao.InsertCategoryDAO;
import br.com.jdbcpp.dao.InsertCategoryDAOImpl;
import br.com.jdbcpp.dao.SelectCategoryDAO;
import br.com.jdbcpp.dao.SelectCategoryDAOImpl;
import br.com.jdbcpp.dto.category.insert.CategoryClassDTO;
import br.com.jdbcpp.dto.category.insert.CategoryClassDTOWithIgnoreProp;
import br.com.jdbcpp.dto.category.insert.CategoryDTO;
import br.com.jdbcpp.util.DatabaseCapability;
import br.com.jdbcpp.util.faker.CustomFaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

abstract class InsertTest {

    private static final CustomFaker customFaker = CustomFaker.getInstance();

    private InsertCategoryDAO insertCategoryDAO;
    private SelectCategoryDAO selectCategoryDAO;

    protected abstract List<DatabaseCapability> capabilities();

    protected boolean supportCapability(final DatabaseCapability capability) {
        return capabilities().contains(capability);
    }

    public abstract DataSource getDataSource() throws SQLException;

    @BeforeEach
    public void setUp() throws SQLException  {
        CustomFaker.getInstance().reseed();
        insertCategoryDAO = new InsertCategoryDAOImpl(getDataSource());
        selectCategoryDAO = new SelectCategoryDAOImpl(getDataSource());
    }

    @Test
    void insertFixData() throws SQLException {
        assertThatNoException().isThrownBy(() -> insertCategoryDAO.insertFixData());

        final var categories = selectCategoryDAO.selectAll();
        assertThat(categories.size()).isOne();
        final var category = categories.getFirst();
        assertThat(category.name()).isEqualTo("food");
        assertThat(category.createdAt()).isNotNull();
        assertThat(category.updatedAt()).isNotNull();

    }

    @Test
    void insertIntRowsAffected() throws SQLException {
        final var actual = insertCategoryDAO.insertFixDataRowsAffectedInt();
        assertThat(actual).isOne();

        final var categories = selectCategoryDAO.selectAll();
        assertThat(categories.size()).isOne();
        final var category = categories.getFirst();
        assertThat(category.name()).isEqualTo("mobília");
        assertThat(category.createdAt()).isNotNull();
        assertThat(category.updatedAt()).isNotNull();
    }

    @Test
    void insertIntegerRowsAffected() throws SQLException {
        final var actual = insertCategoryDAO.insertFixDataRowsAffectedInteger();
        assertThat(actual).isOne();

        final var categories = selectCategoryDAO.selectAll();
        assertThat(categories.size()).isOne();
        final var category = categories.getFirst();
        assertThat(category.name()).isEqualTo("cozinha");
        assertThat(category.createdAt()).isNotNull();
        assertThat(category.updatedAt()).isNotNull();
    }

    @Test
    void insertLongPrimitiveRowsAffected() throws SQLException {
        final var actual = insertCategoryDAO.insertFixDataRowsAffectedLongPrimitive();
        assertThat(actual).isOne();

        final var categories = selectCategoryDAO.selectAll();
        assertThat(categories.size()).isOne();
        final var category = categories.getFirst();
        assertThat(category.name()).isEqualTo("tecnologia");
        assertThat(category.createdAt()).isNotNull();
        assertThat(category.updatedAt()).isNotNull();
    }

    @Test
    void insertILongPrimitiveRowsAffected() throws SQLException {
        final var actual = insertCategoryDAO.insertFixDataRowsAffectedLongClass();
        assertThat(actual).isOne();

        final var categories = selectCategoryDAO.selectAll();
        assertThat(categories.size()).isOne();
        final var category = categories.getFirst();
        assertThat(category.name()).isEqualTo("casa");
        assertThat(category.createdAt()).isNotNull();
        assertThat(category.updatedAt()).isNotNull();
    }

    @Test
    void insertSimpleParamsReturnRecord() throws SQLException {
        final var name = customFaker.lorem().word();
        final var createdAt = OffsetDateTime.ofInstant(customFaker.timeAndDate().past(), UTC);
        final var updatedAt = OffsetDateTime.ofInstant(customFaker.timeAndDate().past(), UTC);

        final var actual = insertCategoryDAO.insertFixDataReturnRecord(name, createdAt, updatedAt);
        final var inserted = selectCategoryDAO.findById(actual);

        assertThat(inserted.name()).isEqualTo(name);
        assertThat(inserted.createdAt()).isNotNull();
        assertThat(inserted.updatedAt()).isNotNull();

    }

    @Test
    void insertClassParamsReturnRecord() throws SQLException {
        final var name = customFaker.lorem().word();
        final var createdAt = OffsetDateTime.ofInstant(customFaker.timeAndDate().past(), UTC);
        final var updatedAt = OffsetDateTime.ofInstant(customFaker.timeAndDate().past(), UTC);

        final var dto = new CategoryDTO(name, createdAt, updatedAt);
        final var actual = insertCategoryDAO.insertFixDataReturnIdByColumnName(dto);
        final var inserted = selectCategoryDAO.findById(actual);

        assertThat(inserted.name()).isEqualTo(name);
        assertThat(inserted.createdAt()).isNotNull();
        assertThat(inserted.updatedAt()).isNotNull();

    }

    @Test
    void insertClassParamsReturnClassGetter() throws SQLException {
        assumeTrue(supportCapability(DatabaseCapability.GENERATED_KEYS_BY_INDEX));
        final var name = customFaker.lorem().word();
        final var createdAt = OffsetDateTime.ofInstant(customFaker.timeAndDate().past(), UTC);
        final var updatedAt = OffsetDateTime.ofInstant(customFaker.timeAndDate().past(), UTC);

        final var dto = new CategoryClassDTO();
        dto.setName(name);
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);
        final var actual = insertCategoryDAO.insertFixDataReturnIdByColumnIndex(dto);
        final var inserted = selectCategoryDAO.findOptionalById(actual);

        assertThat(inserted).isPresent()
                        .hasValueSatisfying(c ->{
                            assertThat(c.getName()).isEqualTo(name);
                            assertThat(c.getCreatedAt()).isNotNull();
                            assertThat(c.getUpdatedAt()).isNotNull();
                        });

    }

    @Test
    void insertClassParamsReturnClassGetterPG() throws SQLException {
        assumeTrue(supportCapability(DatabaseCapability.PG_TEST_CLASS_GETTER_INSERT));
        final var name = customFaker.lorem().word();
        final var createdAt = OffsetDateTime.ofInstant(customFaker.timeAndDate().past(), UTC);
        final var updatedAt = OffsetDateTime.ofInstant(customFaker.timeAndDate().past(), UTC);

        final var dto = new CategoryClassDTO();
        dto.setName(name);
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);
        insertCategoryDAO.insertFixDataClassGetterPG(dto);
        final var inserted = selectCategoryDAO.selectAll();


        assertThat(inserted.size()).isOne();
        final var category = inserted.getFirst();
        assertThat(category.name()).isEqualTo(name);
        assertThat(category.createdAt()).isNotNull();
        assertThat(category.updatedAt()).isNotNull();
    }

    @Test
    void insertClassCustomMapping() {
        final var name = customFaker.lorem().word();
        final var dto = new CategoryClassDTOWithIgnoreProp(name);

        insertCategoryDAO.insertMappedFields(dto);

        final var inserted = selectCategoryDAO.selectAllClassIndex();
        assertThat(inserted.size()).isOne();
        final var category = inserted.getFirst();
        assertThat(category.getName()).isEqualTo(name);
        assertThat(category.getCreatedAt()).isNotNull();
        assertThat(category.getUpdatedAt()).isNotNull();
    }

}
