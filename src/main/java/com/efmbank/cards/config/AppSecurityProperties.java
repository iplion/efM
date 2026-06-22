package com.efmbank.cards.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ConfigurationProperties("app.security")
@Validated
public record AppSecurityProperties(
    @NotBlank
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
    @AssertTrue(message = "cardEncryptionKey must be 16, 24, or 32 UTF-8 bytes for AES")
    public boolean isCardEncryptionKeyValidLength() {
        if (cardEncryptionKey == null) {
            return false;
        }

        int length = cardEncryptionKey.getBytes(StandardCharsets.UTF_8).length;
        return length == 16 || length == 24 || length == 32;
    }
}
