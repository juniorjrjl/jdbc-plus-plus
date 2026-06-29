package br.com.jdbcpp.sample.domain;

import java.time.OffsetDateTime;

public record Employee(
        Long id,
        String name,
        String email,
        OffsetDateTime birthDate
) {
}
