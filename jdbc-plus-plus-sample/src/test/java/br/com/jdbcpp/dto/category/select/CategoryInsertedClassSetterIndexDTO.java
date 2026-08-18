package br.com.jdbcpp.dto.category.select;

import br.com.jdbcpp.api.PropStrategy;

import java.time.OffsetDateTime;

public class CategoryInsertedClassSetterIndexDTO {

    @PropStrategy(ignore = true)
    private Long id;
    @PropStrategy(resultSetIndex = 1)
    private String name;
    @PropStrategy(resultSetIndex = 2)
    private OffsetDateTime createdAt;
    @PropStrategy(resultSetIndex = 3)
    private OffsetDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

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