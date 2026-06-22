package com.efmbank.cards.dto;

import com.efmbank.cards.model.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CardResponseDto(
    UUID publicId,
    Long ownerId,
    String ownerLogin,
    String maskedNumber,
    LocalDate expirationDate,
    CardStatus status,
    BigDecimal balance
) {
}
