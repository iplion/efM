package com.efmbank.cards.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CardBalanceResponseDto(
    UUID cardId,
    BigDecimal balance
) {
}
