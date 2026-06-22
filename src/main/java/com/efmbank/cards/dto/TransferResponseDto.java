package com.efmbank.cards.dto;

import com.efmbank.cards.model.TransferStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferResponseDto(
    UUID transferPublicId,
    UUID sourceCardId,
    UUID targetCardId,
    BigDecimal amount,
    TransferStatus status
) {
}
