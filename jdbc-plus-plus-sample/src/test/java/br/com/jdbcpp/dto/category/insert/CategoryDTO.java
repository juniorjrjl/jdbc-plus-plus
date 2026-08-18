package br.com.jdbcpp.dto.category.insert;

import java.time.OffsetDateTime;

public record CategoryDTO(
        String name,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
