package com.efmbank.cards.controller;

import com.efmbank.cards.dto.CardBalanceResponseDto;
import com.efmbank.cards.dto.CardResponseDto;
import com.efmbank.cards.entity.User;
import com.efmbank.cards.model.CardStatus;
import com.efmbank.cards.security.AuthService;
import com.efmbank.cards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class UserCardController {

    private final CardService cardService;
    private final AuthService authService;

    @GetMapping
    public Page<CardResponseDto> findAll(
        @RequestParam(required = false) CardStatus status,
        Pageable pageable,
        Authentication authentication
    ) {
        User user = authService.getCurrentUser(authentication);
        return cardService.findAll(user.getId(), status, pageable);
    }

    @GetMapping("/{publicId}")
    public CardResponseDto getOwn(@PathVariable UUID publicId, Authentication authentication) {
        User user = authService.getCurrentUser(authentication);
        return cardService.getOwn(publicId, user.getId());
    }

    @GetMapping("/{publicId}/balance")
    public CardBalanceResponseDto getBalance(@PathVariable UUID publicId, Authentication authentication) {
        User user = authService.getCurrentUser(authentication);
        CardResponseDto card = cardService.getOwn(publicId, user.getId());
        return new CardBalanceResponseDto(card.publicId(), card.balance());
    }
}
