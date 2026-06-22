package com.efmbank.cards.controller;

import com.efmbank.cards.dto.CardCreateRequestDto;
import com.efmbank.cards.dto.CardResponseDto;
import com.efmbank.cards.dto.CardUpdateRequestDto;
import com.efmbank.cards.model.CardStatus;
import com.efmbank.cards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {

    private final CardService cardService;

    @PostMapping
    public CardResponseDto create(@Valid @RequestBody CardCreateRequestDto request) {
        return cardService.create(request);
    }

    @GetMapping
    public Page<CardResponseDto> findAll(
        @RequestParam(required = false) Long ownerId,
        @RequestParam(required = false) CardStatus status,
        Pageable pageable
    ) {
        return cardService.findAll(ownerId, status, pageable);
    }

    @GetMapping("/{publicId}")
    public CardResponseDto get(@PathVariable UUID publicId) {
        return cardService.get(publicId);
    }

    @PutMapping("/{publicId}")
    public CardResponseDto update(
        @PathVariable UUID publicId,
        @Valid @RequestBody CardUpdateRequestDto request
    ) {
        return cardService.update(publicId, request);
    }

    @PatchMapping("/{publicId}/activate")
    public CardResponseDto activate(@PathVariable UUID publicId) {
        return cardService.activate(publicId);
    }

    @PatchMapping("/{publicId}/block")
    public CardResponseDto block(@PathVariable UUID publicId) {
        return cardService.block(publicId);
    }

    @DeleteMapping("/{publicId}")
    public void delete(@PathVariable UUID publicId) {
        cardService.delete(publicId);
    }

}
