package br.com.jdbcpp.dto.category.insert;

import br.com.jdbcpp.api.InputParam;

import java.time.OffsetDateTime;

public class CategoryClassDTOWithIgnoreProp {

    @InputParam(statementField = "category_name")
    private String name;
    @InputParam(ignore = true)
    private OffsetDateTime createdAt;
    @InputParam(ignore = true)
    private OffsetDateTime updatedAt;

    public CategoryClassDTOWithIgnoreProp(final String name) {
        this.name = name;
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
