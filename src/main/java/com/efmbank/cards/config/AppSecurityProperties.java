package com.efmbank.cards.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties("app.security")
@Validated
public record AppSecurityProperties(
    @NotBlank
    @Size(min = 32)
    String cardEncryptionKey,

    @NotBlank
    @Size(min = 32)
    String cardHmacKey,

    @NotBlank
    @Size(min = 32)
    String jwtAccessSecret,

    @NotNull
    Duration jwtAccessTtl
) {
}