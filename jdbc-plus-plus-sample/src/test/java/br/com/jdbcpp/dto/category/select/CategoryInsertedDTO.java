package br.com.jdbcpp.dto.category.select;

import java.time.OffsetDateTime;

public record CategoryInsertedDTO(
        Long id,
        String name,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
