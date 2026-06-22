package com.efmbank.cards.service;

import com.efmbank.cards.dto.CardCreateRequestDto;
import com.efmbank.cards.entity.Card;
import com.efmbank.cards.entity.User;
import com.efmbank.cards.exception.CardAlreadyExistsException;
import com.efmbank.cards.exception.CardNotFoundException;
import com.efmbank.cards.exception.UserNotFoundException;
import com.efmbank.cards.model.CardStatus;
import com.efmbank.cards.repository.CardRepository;
import com.efmbank.cards.repository.UserRepository;
import com.efmbank.cards.security.CardNumberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardNumberService cardNumberService;

    @InjectMocks
    private CardService cardService;

    @Test
    void createShouldEncryptHashMaskAndSaveActiveCard() {
        User owner = owner();
        CardCreateRequestDto request = new CardCreateRequestDto(
            owner.getId(),
            "4111111111111234",
            LocalDate.now().plusYears(3),
            new BigDecimal("100.00")
        );

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(cardNumberService.hash(request.cardNumber())).thenReturn("hash");
        when(cardRepository.existsByCardNumberHash("hash")).thenReturn(false);
        when(cardNumberService.encrypt(request.cardNumber())).thenReturn("encrypted");
        when(cardNumberService.lastFourDigits(request.cardNumber())).thenReturn("1234");
        when(cardNumberService.mask("1234")).thenReturn("**** **** **** 1234");
        when(cardRepository.saveAndFlush(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = cardService.create(request);

        assertThat(response.ownerId()).isEqualTo(owner.getId());
        assertThat(response.ownerLogin()).isEqualTo(owner.getLogin());
        assertThat(response.maskedNumber()).isEqualTo("**** **** **** 1234");
        assertThat(response.status()).isEqualTo(CardStatus.ACTIVE);
        assertThat(response.balance()).isEqualByComparingTo("100.00");

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).saveAndFlush(cardCaptor.capture());
        assertThat(cardCaptor.getValue().getPublicId()).isNotNull();
        assertThat(cardCaptor.getValue().getEncryptedCardNumber()).isEqualTo("encrypted");
        assertThat(cardCaptor.getValue().getCardNumberHash()).isEqualTo("hash");
        assertThat(cardCaptor.getValue().getLastFourDigits()).isEqualTo("1234");
    }

    @Test
    void createShouldRejectDuplicateCardNumberHash() {
        User owner = owner();
        CardCreateRequestDto request = new CardCreateRequestDto(
            owner.getId(),
            "4111111111111234",
            LocalDate.now().plusYears(3),
            BigDecimal.ZERO
        );

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(cardNumberService.hash(request.cardNumber())).thenReturn("hash");
        when(cardRepository.existsByCardNumberHash("hash")).thenReturn(true);

        assertThatThrownBy(() -> cardService.create(request))
            .isInstanceOf(CardAlreadyExistsException.class);

        verify(cardRepository, never()).saveAndFlush(any());
    }

    @Test
    void createShouldRejectUnknownOwner() {
        CardCreateRequestDto request = new CardCreateRequestDto(
            99L,
            "4111111111111234",
            LocalDate.now().plusYears(3),
            BigDecimal.ZERO
        );

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.create(request))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void createShouldTranslateUniqueViolationToCardAlreadyExists() {
        User owner = owner();
        CardCreateRequestDto request = new CardCreateRequestDto(
            owner.getId(),
            "4111111111111234",
            LocalDate.now().plusYears(3),
            BigDecimal.ZERO
        );

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(cardNumberService.hash(request.cardNumber())).thenReturn("hash");
        when(cardRepository.existsByCardNumberHash("hash")).thenReturn(false);
        when(cardNumberService.encrypt(request.cardNumber())).thenReturn("encrypted");
        when(cardNumberService.lastFourDigits(request.cardNumber())).thenReturn("1234");
        when(cardRepository.saveAndFlush(any(Card.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> cardService.create(request))
            .isInstanceOf(CardAlreadyExistsException.class);
    }

    @Test
    void delete_shouldMarkCardDeletedWithoutPhysicalDelete() {
        Card card = card();

        when(cardRepository.findByPublicIdAndDeletedFalse(card.getPublicId())).thenReturn(Optional.of(card));

        cardService.delete(card.getPublicId());

        assertThat(card.isDeleted()).isTrue();
        assertThat(card.getDeletedAt()).isNotNull();
        verify(cardRepository).findByPublicIdAndDeletedFalse(eq(card.getPublicId()));
        verify(cardRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrow_whenCardNotFoundOrAlreadyDeleted() {
        UUID publicId = UUID.randomUUID();

        when(cardRepository.findByPublicIdAndDeletedFalse(publicId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.delete(publicId))
            .isInstanceOf(CardNotFoundException.class);

        verify(cardRepository).findByPublicIdAndDeletedFalse(eq(publicId));
        verify(cardRepository, never()).delete(any());
    }

    // --------------------- helpers ---------------------

    private User owner() {
        User owner = new User();
        owner.setId(1L);
        owner.setLogin("user");
        return owner;
    }

    private Card card() {
        Card card = new Card();
        card.setPublicId(UUID.randomUUID());
        card.setOwner(owner());
        card.setLastFourDigits("1234");
        card.setExpirationDate(LocalDate.now().plusYears(1));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        return card;
    }
}
