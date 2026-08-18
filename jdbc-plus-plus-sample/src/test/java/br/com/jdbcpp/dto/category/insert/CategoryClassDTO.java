package br.com.jdbcpp.dto.category.insert;

import java.time.OffsetDateTime;

public class CategoryClassDTO {

    private String name;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
