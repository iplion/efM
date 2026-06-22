package com.efmbank.cards.dto;

import com.efmbank.cards.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateRequestDto(
    @Schema(example = "Updated User")
    @NotBlank
    @Size(max = 80)
    String fullName,

    @Schema(example = "USER")
    @NotNull
    UserRole role,

    @Schema(example = "true")
    boolean enabled
) {
}
