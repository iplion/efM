package com.efmbank.cards.dto;

import com.efmbank.cards.model.BlockRequestStatus;

import java.util.UUID;

public record CardBlockRequestResponseDto(
    Long id,
    UUID cardPublicId,
    String maskedCardNumber,
    BlockRequestStatus status
) {
}
