package br.com.jdbcpp.util.extension;

import org.flywaydb.core.Flyway;
import org.testcontainers.containers.JdbcDatabaseContainer;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.List;

public abstract class DBTestContainerExtension<A extends Annotation, C extends JdbcDatabaseContainer<?>> {

    protected static final List<String> TABLES = List.of("products", "categories", "users", "orders", "payments");

    protected final C container;
    private final Class<A> annotationClass;
    private final Class<C> containerClass;

    protected DBTestContainerExtension(final C container,
                                       final Class<A> annotationClass,
                                       final Class<C> containerClass) {
        this.container = container;
        this.annotationClass = annotationClass;
        this.containerClass = containerClass;
    }

    protected void runFlyway(final String resource) {
        final var flyway = Flyway.configure()
                .dataSource(getDataSource())
                .cleanDisabled(false)
                .locations(resource)
                .load();
        flyway.migrate();
    }

    protected void injectContainerInstance(final Class<?> testClass) throws Exception {
        for (final var field : testClass.getDeclaredFields()) {

            if (Modifier.isStatic(field.getModifiers()) &&
                    field.getType().equals(containerClass) &&
                    field.isAnnotationPresent(annotationClass)) {

                field.setAccessible(true);
                field.set(null, container);
            }
        }
    }

    protected abstract DataSource getDataSource();
}
