package com.efmbank.cards.security;

import com.efmbank.cards.config.AppSecurityProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CardNumberServiceTest {

    private final CardNumberService cardNumberService = new CardNumberService(
        new AppSecurityProperties(
            "0123456789ABCDEF0123456789ABCDEF",
            "0123456789ABCDEF0123456789ABCDEF",
            "0123456789ABCDEF0123456789ABCDEF",
            Duration.ofHours(1)
        )
    );

    @Test
    void encryptShouldNotExposePlainCardNumber() {
        String cardNumber = "4111111111111234";

        String encrypted = cardNumberService.encrypt(cardNumber);

        assertThat(encrypted).isNotBlank();
        assertThat(encrypted).doesNotContain(cardNumber);
    }

    @Test
    void hashShouldBeStableForSameCardNumber() {
        String cardNumber = "4111111111111234";

        String firstHash = cardNumberService.hash(cardNumber);
        String secondHash = cardNumberService.hash(cardNumber);

        assertThat(firstHash).isEqualTo(secondHash);
        assertThat(firstHash).hasSize(64);
    }

    @Test
    void maskShouldUseOnlyLastFourDigits() {
        assertThat(cardNumberService.lastFourDigits("4111111111111234")).isEqualTo("1234");
        assertThat(cardNumberService.mask("1234")).isEqualTo("**** **** **** 1234");
    }
}
