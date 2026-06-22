package com.efmbank.cards.service;

import com.efmbank.cards.dto.TransferCreateRequestDto;
import com.efmbank.cards.dto.TransferResponseDto;
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
import com.efmbank.cards.util.PageableSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferService {

    private static final Set<String> TRANSFER_SORT_PROPERTIES = Set.of(
        "publicId",
        "amount",
        "status",
        "createdAt"
    );

    private final TransferRepository transferRepository;
    private final CardRepository cardRepository;

    @Transactional
    public TransferResponseDto transfer(User owner, TransferCreateRequestDto request) {
        if (transferRepository.findByPublicId(request.publicId()).isPresent()) {
            throw new TransferAlreadyProcessedException();
        }

        try {
            if (request.sourceCardId().equals(request.targetCardId())) {
                throw new InvalidTransferException("Source and target cards must be different");
            }

            List<Card> cards = cardRepository.findOwnedCardsForUpdate(
                List.of(request.sourceCardId(), request.targetCardId()),
                owner.getId()
            );

            if (cards.size() != 2) {
                throw new CardNotFoundException();
            }

            Map<UUID, Card> byPublicId = cards.stream()
                .collect(Collectors.toMap(Card::getPublicId, Function.identity()));

            Card source = byPublicId.get(request.sourceCardId());
            Card target = byPublicId.get(request.targetCardId());

            validateTransferCard(source);
            validateTransferCard(target);

            Transfer transfer = new Transfer();
            transfer.setPublicId(request.publicId());
            transfer.setOwner(owner);
            transfer.setSourceCard(source);
            transfer.setTargetCard(target);
            transfer.setAmount(request.amount());

            if (source.getBalance().compareTo(request.amount()) < 0) {
                transfer.setStatus(TransferStatus.REJECTED);
            } else {
                source.setBalance(source.getBalance().subtract(request.amount()));
                target.setBalance(target.getBalance().add(request.amount()));

                transfer.setStatus(TransferStatus.COMPLETED);
            }

            return toResponse(transferRepository.saveAndFlush(transfer));
        } catch (DataIntegrityViolationException e) {
            throw new TransferAlreadyProcessedException();
        }
    }

    @Transactional(readOnly = true)
    public Page<TransferResponseDto> findByOwner(User owner, Pageable pageable) {
        return transferRepository.findAllByOwnerId(
                owner.getId(),
                PageableSanitizer.sanitize(pageable, TRANSFER_SORT_PROPERTIES)
            )
            .map(this::toResponse);
    }

    private void validateTransferCard(Card card) {
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidTransferException("Only active cards can be used for transfers");
        }

        if (card.getExpirationDate().isBefore(LocalDate.now())) {
            throw new InvalidTransferException("Expired cards cannot be used for transfers");
        }
    }

    private TransferResponseDto toResponse(Transfer transfer) {
        return new TransferResponseDto(
            transfer.getPublicId(),
            transfer.getSourceCard().getPublicId(),
            transfer.getTargetCard().getPublicId(),
            transfer.getAmount(),
            transfer.getStatus()
        );
    }
}
