package br.com.jdbcpp.processor.dto;

import br.com.jdbcpp.processor.dto.method.MethodInfo;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static java.util.Objects.requireNonNull;

public record DAOImplInfo(
        String name,
        String packageName,
        List<MethodInfo> methods
) {

    public static DAOImplInfoBuilder builder() {
        return new DAOImplInfoBuilder();
    }

    public static class DAOImplInfoBuilder {
        @Nullable
        private String name;
        @Nullable
        private String packageName;
        @Nullable
        private List<MethodInfo> methods;

        public DAOImplInfoBuilder name(final String name) {
            this.name = name;
            return this;
        }

        public DAOImplInfoBuilder packageName(final String packageName) {
            this.packageName = packageName;
            return this;
        }

        public DAOImplInfoBuilder methods(final List<MethodInfo> methods) {
            this.methods = methods;
            return this;
        }

        public DAOImplInfo build() {
            return new DAOImplInfo(
                    requireNonNull(name, "name is required"),
                    requireNonNull(packageName, "packageName is required"),
                    requireNonNull(methods, "methods is required")
            );
        }
    }
}
