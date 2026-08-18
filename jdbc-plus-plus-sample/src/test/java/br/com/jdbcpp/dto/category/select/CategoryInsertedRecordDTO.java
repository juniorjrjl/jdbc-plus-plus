package br.com.jdbcpp.dto.category.select;

import java.time.OffsetDateTime;

public record CategoryInsertedRecordDTO(
        String name,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
