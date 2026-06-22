package com.efmbank.cards.dto;

import com.efmbank.cards.model.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardUpdateRequestDto(
    @Schema(example = "2028-12-31")
    @NotNull
    LocalDate expirationDate,

    @Schema(example = "ACTIVE")
    @NotNull
    CardStatus status,

    @Schema(example = "1000.00")
    @NotNull
    @DecimalMin(value = "0.00")
    BigDecimal balance
) {
}
