package com.efmbank.cards.service;

import com.efmbank.cards.entity.Card;
import com.efmbank.cards.entity.CardBlockRequest;
import com.efmbank.cards.entity.User;
import com.efmbank.cards.exception.BlockRequestAlreadyExistsException;
import com.efmbank.cards.exception.BlockRequestAlreadyProcessedException;
import com.efmbank.cards.exception.CardAlreadyBlockedException;
import com.efmbank.cards.exception.CardNotFoundException;
import com.efmbank.cards.model.BlockRequestStatus;
import com.efmbank.cards.model.CardStatus;
import com.efmbank.cards.repository.CardBlockRequestRepository;
import com.efmbank.cards.repository.CardRepository;
import com.efmbank.cards.security.CardNumberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardBlockRequestServiceTest {

    @Mock
    private CardBlockRequestRepository requestRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardNumberService cardNumberService;

    @InjectMocks
    private CardBlockRequestService requestService;

    @Test
    void requestBlockShouldCreateRequestedBlockRequest() {
        User user = user();
        Card card = card(user, CardStatus.ACTIVE);

        when(cardRepository.findByPublicIdAndOwnerIdAndDeletedFalse(card.getPublicId(), user.getId()))
            .thenReturn(Optional.of(card));
        when(requestRepository.existsByCardIdAndStatus(card.getId(), BlockRequestStatus.REQUESTED)).thenReturn(false);
        when(requestRepository.save(any(CardBlockRequest.class))).thenAnswer(invocation -> {
            CardBlockRequest request = invocation.getArgument(0);
            request.setId(10L);
            return request;
        });
        when(cardNumberService.mask(card.getLastFourDigits())).thenReturn("**** **** **** 1234");

        var response = requestService.requestBlock(user, card.getPublicId());

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.cardPublicId()).isEqualTo(card.getPublicId());
        assertThat(response.maskedCardNumber()).isEqualTo("**** **** **** 1234");
        assertThat(response.status()).isEqualTo(BlockRequestStatus.REQUESTED);
    }

    @Test
    void requestBlockShouldRejectUnknownCard() {
        User user = user();
        UUID cardPublicId = UUID.randomUUID();

        when(cardRepository.findByPublicIdAndOwnerIdAndDeletedFalse(cardPublicId, user.getId()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.requestBlock(user, cardPublicId))
            .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    void requestBlockShouldRejectAlreadyBlockedCard() {
        User user = user();
        Card card = card(user, CardStatus.BLOCKED);

        when(cardRepository.findByPublicIdAndOwnerIdAndDeletedFalse(card.getPublicId(), user.getId()))
            .thenReturn(Optional.of(card));

        assertThatThrownBy(() -> requestService.requestBlock(user, card.getPublicId()))
            .isInstanceOf(CardAlreadyBlockedException.class);
    }

    @Test
    void requestBlockShouldRejectDuplicateRequestedBlockRequest() {
        User user = user();
        Card card = card(user, CardStatus.ACTIVE);

        when(cardRepository.findByPublicIdAndOwnerIdAndDeletedFalse(card.getPublicId(), user.getId()))
            .thenReturn(Optional.of(card));
        when(requestRepository.existsByCardIdAndStatus(card.getId(), BlockRequestStatus.REQUESTED)).thenReturn(true);

        assertThatThrownBy(() -> requestService.requestBlock(user, card.getPublicId()))
            .isInstanceOf(BlockRequestAlreadyExistsException.class);
    }

    @Test
    void approveShouldMarkRequestApprovedAndBlockCard() {
        User admin = user();
        Card card = card(user(), CardStatus.ACTIVE);
        CardBlockRequest request = request(card, BlockRequestStatus.REQUESTED);

        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(cardNumberService.mask(card.getLastFourDigits())).thenReturn("**** **** **** 1234");

        var response = requestService.approve(request.getId(), admin);

        assertThat(response.status()).isEqualTo(BlockRequestStatus.APPROVED);
        assertThat(card.getStatus()).isEqualTo(CardStatus.BLOCKED);
        assertThat(request.getProcessedAt()).isNotNull();
        assertThat(request.getProcessedBy()).isSameAs(admin);
    }

    @Test
    void approveShouldRejectAlreadyProcessedRequest() {
        User admin = user();
        CardBlockRequest request = request(card(user(), CardStatus.ACTIVE), BlockRequestStatus.APPROVED);

        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> requestService.approve(request.getId(), admin))
            .isInstanceOf(BlockRequestAlreadyProcessedException.class);
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setLogin("user");
        return user;
    }

    private Card card(User owner, CardStatus status) {
        Card card = new Card();
        card.setId(1L);
        card.setPublicId(UUID.randomUUID());
        card.setOwner(owner);
        card.setLastFourDigits("1234");
        card.setStatus(status);
        return card;
    }

    private CardBlockRequest request(Card card, BlockRequestStatus status) {
        CardBlockRequest request = new CardBlockRequest();
        request.setId(1L);
        request.setCard(card);
        request.setUser(card.getOwner());
        request.setStatus(status);
        return request;
    }
}
