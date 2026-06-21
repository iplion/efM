package com.efmbank.cards.controller;

import com.efmbank.cards.dto.LoginRequestDto;
import com.efmbank.cards.dto.LoginResponseDto;
import com.efmbank.cards.security.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponseDto login(
        @Valid @RequestBody LoginRequestDto request) {
        return  authService.login(request);
    }
}
