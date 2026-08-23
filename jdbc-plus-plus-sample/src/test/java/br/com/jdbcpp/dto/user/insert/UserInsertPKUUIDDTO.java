package br.com.jdbcpp.dto.user.insert;

import java.time.LocalDate;
import java.util.UUID;

public record UserInsertPKUUIDDTO(
        UUID userIdentifier,
        String firstName,
        String lastName,
        String email,
        LocalDate birthDate
) {
}
