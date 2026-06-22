package com.efmbank.cards.controller;

import com.efmbank.cards.dto.UserCreateRequestDto;
import com.efmbank.cards.dto.UserResponseDto;
import com.efmbank.cards.dto.UserUpdateRequestDto;
import com.efmbank.cards.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @PostMapping
    public UserResponseDto create(@Valid @RequestBody UserCreateRequestDto request) {
        return userService.create(request);
    }

    @GetMapping
    public Page<UserResponseDto> findAll(Pageable pageable) {
        return userService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public UserResponseDto get(@PathVariable Long id) {
        return userService.get(id);
    }

    @PutMapping("/{id}")
    public UserResponseDto update(
        @PathVariable Long id,
        @Valid @RequestBody UserUpdateRequestDto request
    ) {
        return userService.update(id, request);
    }

    @PatchMapping("/{id}/enable")
    public UserResponseDto enable(@PathVariable Long id) {
        return userService.enable(id);
    }

    @PatchMapping("/{id}/disable")
    public UserResponseDto disable(@PathVariable Long id) {
        return userService.disable(id);
    }

}
