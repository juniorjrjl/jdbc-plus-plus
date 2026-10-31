package br.com.jdbcpp.dto.category.update;

import java.time.OffsetDateTime;

public record CategoryUpdateDTO(
        Long id,
        String name,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
