package com.efmbank.cards.dto;

import com.efmbank.cards.model.UserRole;

public record UserResponseDto(
    Long id,
    String login,
    String fullName,
    UserRole role,
    boolean enabled
) {
}
