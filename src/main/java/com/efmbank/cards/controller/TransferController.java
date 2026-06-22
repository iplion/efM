package com.efmbank.cards.controller;

import com.efmbank.cards.dto.TransferCreateRequestDto;
import com.efmbank.cards.dto.TransferResponseDto;
import com.efmbank.cards.entity.User;
import com.efmbank.cards.security.AuthService;
import com.efmbank.cards.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class TransferController {

    private final TransferService transferService;
    private final AuthService authService;

    @PostMapping
    public TransferResponseDto transfer(
        @Valid @RequestBody TransferCreateRequestDto request,
        Authentication authentication
    ) {
        User user = authService.getCurrentUser(authentication);
        return transferService.transfer(user, request);
    }

    @GetMapping
    public Page<TransferResponseDto> findByOwner(Pageable pageable, Authentication authentication) {
        User user = authService.getCurrentUser(authentication);
        return transferService.findByOwner(user, pageable);
    }
}
