package br.com.jdbcpp.dto.category.select;

import br.com.jdbcpp.api.output.PropStrategy;

import java.time.OffsetDateTime;

public class CategoryInsertedCustomSetterDTO {

    @PropStrategy(ignore = true)
    private Long id;
    @PropStrategy(resultSetIndex = 1, value = "changeName")
    private String name;
    @PropStrategy(resultSetIndex = 2, value = "changePriority")
    private int priority;
    @PropStrategy(resultSetIndex = 3, value = "changeCreatedAt")
    private OffsetDateTime createdAt;
    @PropStrategy(resultSetIndex = 4, value = "changeUpdatedAt")
    private OffsetDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void changeId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void changeName(final String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public void changePriority(final int priority) {
        this.priority = priority;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void changeCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void changeUpdatedAt(final OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}