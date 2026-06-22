package com.efmbank.cards.security;

import com.efmbank.cards.config.AppSecurityProperties;
import com.efmbank.cards.exception.CardProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class CardNumberService {

    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final String AES = "AES";
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;

    private final AppSecurityProperties props;
    private final SecureRandom secureRandom = new SecureRandom();

    public String encrypt(String cardNumber) {
        try {
            byte[] iv = new byte[GCM_IV_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(cardNumber.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);

            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (GeneralSecurityException e) {
            throw new CardProcessingException(e);
        }
    }

    public String hash(String cardNumber) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(props.cardHmacKey().getBytes(StandardCharsets.UTF_8), HMAC_SHA256));

            return HexFormat.of().formatHex(mac.doFinal(cardNumber.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new CardProcessingException(e);
        }
    }

    public String lastFourDigits(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            throw new CardProcessingException("Invalid card number");
        }

        return cardNumber.substring(cardNumber.length() - 4);
    }

    public String mask(String lastFourDigits) {
        return "**** **** **** " + lastFourDigits;
    }

    private SecretKeySpec aesKey() {
        return new SecretKeySpec(props.cardEncryptionKey().getBytes(StandardCharsets.UTF_8), AES);
    }

    // расшифровка номера карты.
//    public String decrypt(String encryptedCardNumber) {
//        try {
//            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedCardNumber);
//
//            ByteBuffer buffer = ByteBuffer.wrap(encryptedBytes);
//
//            byte[] iv = new byte[GCM_IV_BYTES];
//            buffer.get(iv);
//
//            byte[] encrypted = new byte[buffer.remaining()];
//            buffer.get(encrypted);
//
//            Cipher cipher = Cipher.getInstance(AES_GCM);
//            cipher.init(
//                Cipher.DECRYPT_MODE,
//                aesKey(),
//                new GCMParameterSpec(GCM_TAG_BITS, iv)
//            );
//
//            byte[] decrypted = cipher.doFinal(encrypted);
//
//            return new String(decrypted, StandardCharsets.UTF_8);
//        } catch (GeneralSecurityException | IllegalArgumentException e) {
//            throw new CardProcessingException(e);
//        }
//    }

}
