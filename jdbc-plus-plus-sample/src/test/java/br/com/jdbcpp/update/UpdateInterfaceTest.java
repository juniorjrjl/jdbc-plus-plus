package br.com.jdbcpp.update;

import br.com.jdbcpp.dao.interfaces.InsertCategoryDAO;
import br.com.jdbcpp.dao.interfaces.InsertCategoryDAOImpl;
import br.com.jdbcpp.dao.interfaces.SelectCategoryDAO;
import br.com.jdbcpp.dao.interfaces.SelectCategoryDAOImpl;
import br.com.jdbcpp.dao.interfaces.UpdateCategoryDAO;
import br.com.jdbcpp.dao.interfaces.UpdateCategoryDAOImpl;
import br.com.jdbcpp.dto.category.CategoryTypeEnum;
import br.com.jdbcpp.util.DatabaseCapability;
import br.com.jdbcpp.util.faker.CustomFaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

abstract class UpdateInterfaceTest {

    private static final CustomFaker customFaker = CustomFaker.getInstance();

    private UpdateCategoryDAO updateCategoryDAO;
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
        updateCategoryDAO = new UpdateCategoryDAOImpl(getDataSource());
        insertCategoryDAO = new InsertCategoryDAOImpl(getDataSource());
        selectCategoryDAO = new SelectCategoryDAOImpl(getDataSource());
    }

    @Test
    void updateFixData() throws SQLException, InterruptedException {
        assertThatNoException().isThrownBy(() -> insertCategoryDAO.insertFixData());

        final var categoriesAfterInsert = selectCategoryDAO.selectAll();
        assertThat(categoriesAfterInsert.size()).isOne();
        final var categoryInserted = categoriesAfterInsert.getFirst();

        TimeUnit.SECONDS.sleep(1);
        updateCategoryDAO.updateFixData();
        final var categoriesAfterUpdate = selectCategoryDAO.selectAll();
        assertThat(categoriesAfterUpdate.size()).isOne();
        final var categoryUpdated = categoriesAfterUpdate.getFirst();

        assertThat(categoryUpdated.name()).isEqualTo("food changed");
        assertThat(categoryUpdated.createdAt()).isNotNull();
        assertThat(categoryUpdated.updatedAt()).isAfter(categoryInserted.updatedAt());

    }

    @Test
    void updateIntRowsAffected() throws SQLException, InterruptedException {
        assertThatNoException().isThrownBy(() -> insertCategoryDAO.insertFixData());

        final var categoriesAfterInsert = selectCategoryDAO.selectAll();
        assertThat(categoriesAfterInsert.size()).isOne();
        final var categoryInserted = categoriesAfterInsert.getFirst();

        TimeUnit.SECONDS.sleep(1);
        final var actual = updateCategoryDAO.updateFixDataRowsAffectedInt();
        assertThat(actual).isOne();

        final var categoriesAfterUpdate = selectCategoryDAO.selectAll();
        assertThat(categoriesAfterUpdate.size()).isOne();
        final var categoryUpdated = categoriesAfterUpdate.getFirst();
        assertThat(categoryUpdated.name()).isEqualTo("mobília changed");
        assertThat(categoryUpdated.createdAt()).isNotNull();
        assertThat(categoryUpdated.updatedAt()).isAfter(categoryInserted.updatedAt());
    }

    @Test
    void updateIntegerRowsAffected() throws SQLException, InterruptedException {
        assertThatNoException().isThrownBy(() -> insertCategoryDAO.insertFixData());

        final var categoriesAfterInsert = selectCategoryDAO.selectAll();
        assertThat(categoriesAfterInsert.size()).isOne();
        final var categoryInserted = categoriesAfterInsert.getFirst();

        TimeUnit.SECONDS.sleep(1);
        final var actual = updateCategoryDAO.updateFixDataRowsAffectedInteger();
        assertThat(actual).isOne();

        final var categoriesAfterUpdate = selectCategoryDAO.selectAll();
        assertThat(categoriesAfterUpdate.size()).isOne();
        final var categoryUpdated = categoriesAfterUpdate.getFirst();
        assertThat(categoryUpdated.name()).isEqualTo("cozinha changed");
        assertThat(categoryUpdated.createdAt()).isNotNull();
        assertThat(categoryUpdated.updatedAt()).isAfter(categoryInserted.updatedAt());
    }

    @Test
    void updateLongPrimitiveRowsAffected() throws SQLException, InterruptedException {
        assertThatNoException().isThrownBy(() -> insertCategoryDAO.insertFixData());

        final var categoriesAfterInsert = selectCategoryDAO.selectAll();
        assertThat(categoriesAfterInsert.size()).isOne();
        final var categoryInserted = categoriesAfterInsert.getFirst();

        TimeUnit.SECONDS.sleep(1);
        final var actual = updateCategoryDAO.updateFixDataRowsAffectedLongPrimitive();
        assertThat(actual).isOne();

        final var categoriesAfterUpdate = selectCategoryDAO.selectAll();
        assertThat(categoriesAfterUpdate.size()).isOne();
        final var categoryUpdated = categoriesAfterUpdate.getFirst();
        assertThat(categoryUpdated.name()).isEqualTo("tecnologia changed");
        assertThat(categoryUpdated.createdAt()).isNotNull();
        assertThat(categoryUpdated.updatedAt()).isAfter(categoryInserted.updatedAt());
    }

    @Test
    void updateLongClassRowsAffected() throws SQLException, InterruptedException {
        assertThatNoException().isThrownBy(() -> insertCategoryDAO.insertFixData());

        final var categoriesAfterInsert = selectCategoryDAO.selectAll();
        assertThat(categoriesAfterInsert.size()).isOne();
        final var categoryInserted = categoriesAfterInsert.getFirst();

        TimeUnit.SECONDS.sleep(1);
        final var actual = updateCategoryDAO.updateFixDataRowsAffectedLongClass();
        assertThat(actual).isOne();

        final var categoriesAfterUpdate = selectCategoryDAO.selectAll();
        assertThat(categoriesAfterUpdate.size()).isOne();
        final var categoryUpdated = categoriesAfterUpdate.getFirst();
        assertThat(categoryUpdated.name()).isEqualTo("casa changed");
        assertThat(categoryUpdated.createdAt()).isNotNull();
        assertThat(categoryUpdated.updatedAt()).isAfter(categoryInserted.updatedAt());
    }

    @Test
    void updateSimpleParamsReturnRecord() throws SQLException {
        assertThatNoException().isThrownBy(() -> insertCategoryDAO.insertFixData());

        final var categoriesAfterInsert = selectCategoryDAO.selectAllWithCustomImpl();
        assertThat(categoriesAfterInsert.size()).isOne();
        final var categoryInserted = categoriesAfterInsert.getFirst();
        final var id = categoryInserted.getId();

        final var name = customFaker.lorem().word();
        final var updatedAt = OffsetDateTime.now();
        final var actual = updateCategoryDAO.updateFixDataReturnRecord(name, updatedAt, id);
        assertThat(actual).isOne();

        final var categoryUpdated = selectCategoryDAO.findById(id);
        assertThat(categoryUpdated.name()).isEqualTo(name);
        assertThat(categoryUpdated.createdAt()).isNotNull();
        assertThat(categoryUpdated.updatedAt())
                .isCloseTo(updatedAt, within(1, ChronoUnit.SECONDS));
    }

    @Test
    void updateEnumIntAndStringValues() throws SQLException {
        final var categoryEnum = customFaker.options().option(CategoryTypeEnum.class);

        assertThatNoException().isThrownBy(() -> insertCategoryDAO.insertInsertEnumToStringAndInt(categoryEnum, categoryEnum));

        final var categoriesAfterInsert = selectCategoryDAO.selectAllWithPriority();
        assertThat(categoriesAfterInsert.size()).isOne();
        final var categoryInserted = categoriesAfterInsert.iterator().next();
        final var id = categoryInserted.getId();

        updateCategoryDAO.updateEnumToStringAndInt(categoryEnum, categoryEnum, id);
        final var categoriesAfterUpdate = selectCategoryDAO.selectAllWithPriority();
        assertThat(categoriesAfterUpdate.size()).isOne();
        final var categoryUpdated = categoriesAfterUpdate.iterator().next();
        assertThat(categoryUpdated.getName()).isEqualTo(categoryEnum.name());
        assertThat(categoryUpdated.getPriority()).isEqualTo(categoryEnum.ordinal());
    }

    @Test
    void updateEnumCustomMethod() throws SQLException, InterruptedException {
        final var categoryEnum = customFaker.options().option(CategoryTypeEnum.class);

        assertThatNoException().isThrownBy(() -> insertCategoryDAO.insertInsertEnumCustomMethod(categoryEnum));

        final var categoriesAfterInsert = selectCategoryDAO.selectAllWithSetCustomImpl();
        assertThat(categoriesAfterInsert.size()).isOne();
        final var categoryInserted = categoriesAfterInsert.stream().iterator().next();
        final var id = categoryInserted.getId();

        updateCategoryDAO.updateEnumCustomMethod(categoryEnum, id);
        final var categoriesAfterUpdate = selectCategoryDAO.selectAll();
        assertThat(categoriesAfterUpdate.size()).isOne();
        final var categoryUpdated = categoriesAfterUpdate.getFirst();
        assertThat(categoryUpdated.name()).isEqualTo(categoryEnum.getEnumNameLowerCase());
    }

}
