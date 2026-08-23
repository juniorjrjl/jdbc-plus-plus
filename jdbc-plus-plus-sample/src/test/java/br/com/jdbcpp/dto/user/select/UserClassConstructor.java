package br.com.jdbcpp.dto.user.select;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class UserClassConstructor {

    private final UUID id;
    private final UUID userIdentifier;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final LocalDate birthDate;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public UserClassConstructor(final UUID id,
                                final UUID userIdentifier,
                                final String firstName,
                                final String lastName,
                                final String email,
                                final LocalDate birthDate,
                                final OffsetDateTime createdAt,
                                final OffsetDateTime updatedAt) {
        this.id = id;
        this.userIdentifier = userIdentifier;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserIdentifier() {
        return userIdentifier;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
