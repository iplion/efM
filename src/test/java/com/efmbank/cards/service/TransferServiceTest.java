package com.efmbank.cards.service;

import com.efmbank.cards.dto.TransferCreateRequestDto;
import com.efmbank.cards.entity.Card;
import com.efmbank.cards.entity.Transfer;
import com.efmbank.cards.entity.User;
import com.efmbank.cards.exception.CardNotFoundException;
import com.efmbank.cards.exception.InvalidTransferException;
import com.efmbank.cards.exception.TransferAlreadyProcessedException;
import com.efmbank.cards.model.CardStatus;
import com.efmbank.cards.model.TransferStatus;
import com.efmbank.cards.repository.CardRepository;
import com.efmbank.cards.repository.TransferRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private TransferService transferService;

    @Test
    void transferShouldMoveMoneyBetweenOwnActiveCards() {
        User owner = user();
        UUID transferPublicId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        Card source = card(sourceId, owner, "100.00", CardStatus.ACTIVE);
        Card target = card(targetId, owner, "25.00", CardStatus.ACTIVE);
        TransferCreateRequestDto request = new TransferCreateRequestDto(
            transferPublicId,
            sourceId,
            targetId,
            new BigDecimal("30.00")
        );

        when(transferRepository.findByPublicId(transferPublicId)).thenReturn(Optional.empty());
        when(cardRepository.findOwnedCardsForUpdate(List.of(sourceId, targetId), owner.getId()))
            .thenReturn(List.of(source, target));
        when(transferRepository.saveAndFlush(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        transferService.transfer(owner, request);

        assertThat(source.getBalance()).isEqualByComparingTo("70.00");
        assertThat(target.getBalance()).isEqualByComparingTo("55.00");

        ArgumentCaptor<Transfer> transferCaptor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).saveAndFlush(transferCaptor.capture());
        assertThat(transferCaptor.getValue().getPublicId()).isEqualTo(transferPublicId);
        assertThat(transferCaptor.getValue().getStatus()).isEqualTo(TransferStatus.COMPLETED);
    }

    @Test
    void transferShouldRejectRepeatedPublicId() {
        User owner = user();
        UUID transferPublicId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        Card source = card(sourceId, owner, "100.00", CardStatus.ACTIVE);
        Card target = card(targetId, owner, "25.00", CardStatus.ACTIVE);
        Transfer existingTransfer = new Transfer();
        existingTransfer.setPublicId(transferPublicId);
        existingTransfer.setOwner(owner);
        existingTransfer.setSourceCard(source);
        existingTransfer.setTargetCard(target);
        existingTransfer.setAmount(new BigDecimal("30.00"));
        existingTransfer.setStatus(TransferStatus.COMPLETED);
        TransferCreateRequestDto request = new TransferCreateRequestDto(
            transferPublicId,
            sourceId,
            targetId,
            new BigDecimal("30.00")
        );

        when(transferRepository.findByPublicId(transferPublicId)).thenReturn(Optional.of(existingTransfer));

        assertThatThrownBy(() -> transferService.transfer(owner, request))
            .isInstanceOf(TransferAlreadyProcessedException.class)
            .hasMessageContaining("Transfer already processed");

        assertThat(source.getBalance()).isEqualByComparingTo("100.00");
        assertThat(target.getBalance()).isEqualByComparingTo("25.00");
        verify(transferRepository, never()).saveAndFlush(any());
    }

    @Test
    void transferShouldRejectWhenConcurrentRequestAlreadyInsertedPublicId() {
        User owner = user();
        UUID transferPublicId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        Card source = card(sourceId, owner, "100.00", CardStatus.ACTIVE);
        Card target = card(targetId, owner, "25.00", CardStatus.ACTIVE);
        TransferCreateRequestDto request = new TransferCreateRequestDto(
            transferPublicId,
            sourceId,
            targetId,
            new BigDecimal("30.00")
        );

        when(transferRepository.findByPublicId(transferPublicId)).thenReturn(Optional.empty());
        when(cardRepository.findOwnedCardsForUpdate(List.of(sourceId, targetId), owner.getId()))
            .thenReturn(List.of(source, target));
        when(transferRepository.saveAndFlush(any(Transfer.class)))
            .thenThrow(new DataIntegrityViolationException("duplicate public_id"));

        assertThatThrownBy(() -> transferService.transfer(owner, request))
            .isInstanceOf(TransferAlreadyProcessedException.class)
            .hasMessageContaining("Transfer already processed");
    }

    @Test
    void transferShouldSaveRejectedTransferWhenNotEnoughMoney() {
        User owner = user();
        UUID transferPublicId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        Card source = card(sourceId, owner, "10.00", CardStatus.ACTIVE);
        Card target = card(targetId, owner, "25.00", CardStatus.ACTIVE);
        TransferCreateRequestDto request = new TransferCreateRequestDto(
            transferPublicId,
            sourceId,
            targetId,
            new BigDecimal("30.00")
        );

        when(transferRepository.findByPublicId(transferPublicId)).thenReturn(Optional.empty());
        when(cardRepository.findOwnedCardsForUpdate(List.of(sourceId, targetId), owner.getId()))
            .thenReturn(List.of(source, target));
        when(transferRepository.saveAndFlush(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        transferService.transfer(owner, request);

        assertThat(source.getBalance()).isEqualByComparingTo("10.00");
        assertThat(target.getBalance()).isEqualByComparingTo("25.00");

        ArgumentCaptor<Transfer> transferCaptor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).saveAndFlush(transferCaptor.capture());
        assertThat(transferCaptor.getValue().getStatus()).isEqualTo(TransferStatus.REJECTED);
    }

    @Test
    void transferShouldRejectBlockedCards() {
        User owner = user();
        UUID transferPublicId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        Card source = card(sourceId, owner, "100.00", CardStatus.BLOCKED);
        Card target = card(targetId, owner, "25.00", CardStatus.ACTIVE);
        TransferCreateRequestDto request = new TransferCreateRequestDto(
            transferPublicId,
            sourceId,
            targetId,
            new BigDecimal("30.00")
        );

        when(transferRepository.findByPublicId(transferPublicId)).thenReturn(Optional.empty());
        when(cardRepository.findOwnedCardsForUpdate(List.of(sourceId, targetId), owner.getId()))
            .thenReturn(List.of(source, target));

        assertThatThrownBy(() -> transferService.transfer(owner, request))
            .isInstanceOf(InvalidTransferException.class)
            .hasMessageContaining("Only active cards");
    }

    @Test
    void transfer_shouldThrow_whenOneCardIsNotAvailable() {
        User owner = user();
        UUID transferPublicId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        Card target = card(targetId, owner, "25.00", CardStatus.ACTIVE);
        TransferCreateRequestDto request = new TransferCreateRequestDto(
            transferPublicId,
            sourceId,
            targetId,
            new BigDecimal("30.00")
        );

        when(transferRepository.findByPublicId(transferPublicId)).thenReturn(Optional.empty());
        when(cardRepository.findOwnedCardsForUpdate(List.of(sourceId, targetId), owner.getId()))
            .thenReturn(List.of(target));

        assertThatThrownBy(() -> transferService.transfer(owner, request))
            .isInstanceOf(CardNotFoundException.class);

        verify(cardRepository).findOwnedCardsForUpdate(
            eq(List.of(sourceId, targetId)),
            eq(owner.getId())
        );
        verify(transferRepository, never()).saveAndFlush(any());
    }

    // --------------------- helpers ---------------------

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setLogin("user");
        return user;
    }

    private Card card(
        UUID publicId,
        User owner,
        String balance,
        CardStatus status
    ) {
        Card card = new Card();
        card.setPublicId(publicId);
        card.setOwner(owner);
        card.setBalance(new BigDecimal(balance));
        card.setExpirationDate(LocalDate.now().plusYears(1));
        card.setStatus(status);

        return card;
    }
}
