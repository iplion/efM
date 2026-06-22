package com.efmbank.cards.dto;

import com.efmbank.cards.model.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequestDto(
    @Schema(example = "newuser")
    @NotBlank
    @Size(max = 30)
    String login,

    @Schema(example = "password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank
    @Size(max = 150)
    String password,

    @Schema(example = "Test User")
    @NotBlank
    @Size(max = 80)
    String fullName,

    @Schema(example = "USER")
    @NotNull
    UserRole role,

    @Schema(example = "true")
    boolean enabled
) {
    @Override
    public String toString() {
        return "UserCreateRequestDto[login=%s, password=***, fullName=%s, role=%s, enabled=%s]"
            .formatted(login, fullName, role, enabled);
    }
}
