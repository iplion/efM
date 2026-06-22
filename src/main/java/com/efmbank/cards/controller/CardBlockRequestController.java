package com.efmbank.cards.controller;

import com.efmbank.cards.dto.CardBlockRequestResponseDto;
import com.efmbank.cards.entity.User;
import com.efmbank.cards.security.AuthService;
import com.efmbank.cards.service.CardBlockRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/card-block-requests")
@RequiredArgsConstructor
public class CardBlockRequestController {

    private final CardBlockRequestService requestService;
    private final AuthService authService;

    @PostMapping("/cards/{cardPublicId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public CardBlockRequestResponseDto requestBlock(
        @PathVariable UUID cardPublicId,
        Authentication authentication
    ) {
        User user = authService.getCurrentUser(authentication);
        return requestService.requestBlock(user, cardPublicId);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public Page<CardBlockRequestResponseDto> findOwn(Pageable pageable, Authentication authentication) {
        User user = authService.getCurrentUser(authentication);
        return requestService.findByOwner(user, pageable);
    }

    @GetMapping("/requested")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardBlockRequestResponseDto> findRequested(Pageable pageable) {
        return requestService.findRequested(pageable);
    }

    @PatchMapping("/{requestId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public CardBlockRequestResponseDto approve(
        @PathVariable Long requestId,
        Authentication authentication
    ) {
        User admin = authService.getCurrentUser(authentication);
        return requestService.approve(requestId, admin);
    }

    @PatchMapping("/{requestId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public CardBlockRequestResponseDto reject(
        @PathVariable Long requestId,
        Authentication authentication
    ) {
        User admin = authService.getCurrentUser(authentication);
        return requestService.reject(requestId, admin);
    }
}
