package com.efmbank.cards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferCreateRequestDto(
    @Schema(example = "11111111-1111-4111-8111-111111111111")
    @NotNull
    UUID publicId,

    @Schema(example = "22222222-2222-4222-8222-222222222222")
    @NotNull
    UUID sourceCardId,

    @Schema(example = "33333333-3333-4333-8333-333333333333")
    @NotNull
    UUID targetCardId,

    @Schema(example = "100.00")
    @NotNull
    @DecimalMin(value = "0.01")
    BigDecimal amount
) {
}
