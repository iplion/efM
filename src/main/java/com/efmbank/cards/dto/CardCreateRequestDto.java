package com.efmbank.cards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardCreateRequestDto(
    @Schema(example = "2")
    @NotNull
    @Positive
    Long ownerId,

    @Schema(example = "4111111111111234")
    @NotBlank
    @Pattern(regexp = "\\d{16,19}", message = "Card number must contain 16-19 digits")
    String cardNumber,

    @Schema(example = "2028-12-31")
    @NotNull
    @Future
    LocalDate expirationDate,

    @Schema(example = "1000.00")
    @NotNull
    @DecimalMin(value = "0.00")
    BigDecimal balance
) {
}
