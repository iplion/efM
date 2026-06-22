package com.efmbank.cards.security;

import com.efmbank.cards.dto.LoginRequestDto;
import com.efmbank.cards.dto.LoginResponseDto;
import com.efmbank.cards.entity.User;
import com.efmbank.cards.exception.UnauthorizedException;
import com.efmbank.cards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public LoginResponseDto login(LoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.login(),
                request.password()
            )
        );

        return new LoginResponseDto(jwtService.generateToken(authentication));
    }

    @Transactional(readOnly = true)
    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException();
        }

        return userRepository.findByLogin(authentication.getName())
            .orElseThrow(UnauthorizedException::new);
    }

}