package br.com.jdbcpp.delete.interfaces;

import br.com.jdbcpp.dao.interfaces.DeleteCategoryDAO;
import br.com.jdbcpp.dao.interfaces.DeleteCategoryDAOImpl;
import br.com.jdbcpp.dao.interfaces.InsertCategoryDAO;
import br.com.jdbcpp.dao.interfaces.InsertCategoryDAOImpl;
import br.com.jdbcpp.dao.interfaces.SelectCategoryDAO;
import br.com.jdbcpp.dao.interfaces.SelectCategoryDAOImpl;
import br.com.jdbcpp.dto.category.select.CategoryInsertedClassDTO;
import br.com.jdbcpp.util.DatabaseCapability;
import br.com.jdbcpp.util.faker.CustomFaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class DeleteInterfaceTest {

    private static final CustomFaker customFaker = CustomFaker.getInstance();

    private InsertCategoryDAO insertCategoryDAO;
    private SelectCategoryDAO selectCategoryDAO;
    private DeleteCategoryDAO deleteCategoryDAO;

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
        deleteCategoryDAO = new DeleteCategoryDAOImpl(getDataSource());
    }

    @Test
    void deleteFixData() throws SQLException {
        final var amount = customFaker.number().randomDigitNotZero();
        for (int i = 0; i < amount; i++) {
                insertCategoryDAO.insertFixData();
        }

        deleteCategoryDAO.deleteFixData();

        assertThat(selectCategoryDAO.selectAll()).isEmpty();
    }

    @Test
    void deleteIntRowsAffected() throws SQLException {
        final var amount = customFaker.number().randomDigitNotZero();
        for (int i = 0; i < amount; i++) {
            insertCategoryDAO.insertFixData();
        }

        final var rowsAffected = deleteCategoryDAO.deleteFixDataRowsAffectedInt();

        assertThat(rowsAffected).isEqualTo(amount);
        assertThat(selectCategoryDAO.selectAll()).isEmpty();
    }

    @Test
    void deleteIntegerRowsAffected() throws SQLException {
        final var amount = customFaker.number().randomDigitNotZero();
        for (int i = 0; i < amount; i++) {
            insertCategoryDAO.insertFixData();
        }

        final var rowsAffected = deleteCategoryDAO.deleteFixDataRowsAffectedInteger();

        assertThat(rowsAffected).isEqualTo(amount);
        assertThat(selectCategoryDAO.selectAll()).isEmpty();
    }

    @Test
    void deleteLongPrimitiveRowsAffected() throws SQLException {
        final var amount = customFaker.number().randomDigitNotZero();
        for (int i = 0; i < amount; i++) {
            insertCategoryDAO.insertFixData();
        }

        final var rowsAffected = deleteCategoryDAO.deleteFixDataRowsAffectedLongPrimitive();

        assertThat(rowsAffected).isEqualTo(amount);
        assertThat(selectCategoryDAO.selectAll()).isEmpty();
    }

    @Test
    void deleteLongClassRowsAffected() throws SQLException {
        final var amount = customFaker.number().randomDigitNotZero();
        for (int i = 0; i < amount; i++) {
            insertCategoryDAO.insertFixData();
        }

        final var rowsAffected = deleteCategoryDAO.deleteFixDataRowsAffectedLongClass();

        assertThat(rowsAffected).isEqualTo(amount);
        assertThat(selectCategoryDAO.selectAll()).isEmpty();
    }

    @Test
    void deleteById() throws SQLException {
        final var amount = customFaker.number().randomDigitNotZero();
        for (int i = 0; i < amount; i++) {
            insertCategoryDAO.insertFixData();
        }

        final var ids = selectCategoryDAO.selectAllWithCustomImpl().stream()
                .map(CategoryInsertedClassDTO::getId)
                .toList();

        for (Long id : ids) {
            deleteCategoryDAO.deleteById(id);
        }

        assertThat(selectCategoryDAO.selectAll()).isEmpty();
    }

    @Test
    void deleteByIdsList() throws SQLException {
        final var amount = customFaker.number().randomDigitNotZero();
        for (int i = 0; i < amount; i++) {
            insertCategoryDAO.insertFixData();
        }

        final var ids = selectCategoryDAO.selectAllWithCustomImpl().stream()
                .map(CategoryInsertedClassDTO::getId)
                .toList();

        deleteCategoryDAO.deleteByIdsList(ids);

        assertThat(selectCategoryDAO.selectAll()).isEmpty();
    }

    @Test
    void deleteByIdsSet() throws SQLException {
        final var amount = customFaker.number().randomDigitNotZero();
        for (int i = 0; i < amount; i++) {
            insertCategoryDAO.insertFixData();
        }

        final var ids = selectCategoryDAO.selectAllWithCustomImpl().stream()
                .map(CategoryInsertedClassDTO::getId)
                .collect(Collectors.toSet());

        deleteCategoryDAO.deleteByIdsSet(ids);

        assertThat(selectCategoryDAO.selectAll()).isEmpty();
    }

    @Test
    void deleteByIdsArray() throws SQLException {
        final var amount = customFaker.number().randomDigitNotZero();
        for (int i = 0; i < amount; i++) {
            insertCategoryDAO.insertFixData();
        }

        final var ids = selectCategoryDAO.selectAllWithCustomImpl().stream()
                .map(CategoryInsertedClassDTO::getId)
                .toArray(Long[]::new);

        deleteCategoryDAO.deleteByIdsArray(ids);

        assertThat(selectCategoryDAO.selectAll()).isEmpty();
    }

}
