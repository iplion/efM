package com.efmbank.cards.service;

import com.efmbank.cards.dto.CardCreateRequestDto;
import com.efmbank.cards.dto.CardResponseDto;
import com.efmbank.cards.dto.CardUpdateRequestDto;
import com.efmbank.cards.entity.Card;
import com.efmbank.cards.entity.User;
import com.efmbank.cards.exception.CardAlreadyExistsException;
import com.efmbank.cards.exception.CardNotFoundException;
import com.efmbank.cards.exception.UserNotFoundException;
import com.efmbank.cards.model.CardStatus;
import com.efmbank.cards.repository.CardRepository;
import com.efmbank.cards.repository.UserRepository;
import com.efmbank.cards.security.CardNumberService;
import com.efmbank.cards.util.PageableSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private static final Set<String> CARD_SORT_PROPERTIES = Set.of(
        "publicId",
        "expirationDate",
        "status",
        "balance",
        "createdAt",
        "updatedAt"
    );

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardNumberService cardNumberService;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponseDto create(CardCreateRequestDto request) {
        User owner = userRepository.findById(request.ownerId())
            .orElseThrow(UserNotFoundException::new);

        String cardNumberHash = cardNumberService.hash(request.cardNumber());
        if (cardRepository.existsByCardNumberHash(cardNumberHash)) {
            throw new CardAlreadyExistsException();
        }

        Card card = new Card();
        card.setPublicId(UUID.randomUUID());
        card.setOwner(owner);
        card.setEncryptedCardNumber(cardNumberService.encrypt(request.cardNumber()));
        card.setCardNumberHash(cardNumberHash);
        card.setLastFourDigits(cardNumberService.lastFourDigits(request.cardNumber()));
        card.setExpirationDate(request.expirationDate());
        card.setBalance(request.balance());
        card.setStatus(CardStatus.ACTIVE);

        try {
            return toResponse(cardRepository.saveAndFlush(card));
        } catch (DataIntegrityViolationException e) {
            throw new CardAlreadyExistsException();
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponseDto get(UUID publicId) {
        return toResponse(getCard(publicId));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponseDto update(UUID publicId, CardUpdateRequestDto request) {
        Card card = getCard(publicId);
        card.setExpirationDate(request.expirationDate());
        card.setStatus(request.status());
        card.setBalance(request.balance());

        return toResponse(card);
    }

    @Transactional(readOnly = true)
    public Page<CardResponseDto> findAll(Long ownerId, CardStatus status, Pageable pageable) {
        return cardRepository.findAllCards(ownerId, status, PageableSanitizer.sanitize(pageable, CARD_SORT_PROPERTIES))
            .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CardResponseDto getOwn(UUID publicId, Long ownerId) {
        return toResponse(
            cardRepository.findByPublicIdAndOwnerIdAndDeletedFalse(publicId, ownerId)
                .orElseThrow(CardNotFoundException::new)
        );
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponseDto activate(UUID publicId) {
        Card card = getCard(publicId);
        card.setStatus(CardStatus.ACTIVE);
        return toResponse(card);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponseDto block(UUID publicId) {
        Card card = getCard(publicId);
        card.setStatus(CardStatus.BLOCKED);
        return toResponse(card);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(UUID publicId) {
        Card card = getCard(publicId);
        card.setDeleted(true);
        card.setDeletedAt(Instant.now());
    }

    public CardResponseDto toResponse(Card card) {
        return new CardResponseDto(
            card.getPublicId(),
            card.getOwner().getId(),
            card.getOwner().getLogin(),
            cardNumberService.mask(card.getLastFourDigits()),
            card.getExpirationDate(),
            card.getStatus(),
            card.getBalance()
        );
    }

    private Card getCard(UUID publicId) {
        return cardRepository.findByPublicIdAndDeletedFalse(publicId)
            .orElseThrow(CardNotFoundException::new);
    }

}
