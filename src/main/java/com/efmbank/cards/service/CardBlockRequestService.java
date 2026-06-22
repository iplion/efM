package com.efmbank.cards.service;

import com.efmbank.cards.dto.CardBlockRequestResponseDto;
import com.efmbank.cards.entity.Card;
import com.efmbank.cards.entity.CardBlockRequest;
import com.efmbank.cards.entity.User;
import com.efmbank.cards.exception.BlockRequestAlreadyExistsException;
import com.efmbank.cards.exception.BlockRequestAlreadyProcessedException;
import com.efmbank.cards.exception.BlockRequestNotFoundException;
import com.efmbank.cards.exception.CardAlreadyBlockedException;
import com.efmbank.cards.exception.CardNotFoundException;
import com.efmbank.cards.model.BlockRequestStatus;
import com.efmbank.cards.model.CardStatus;
import com.efmbank.cards.repository.CardBlockRequestRepository;
import com.efmbank.cards.repository.CardRepository;
import com.efmbank.cards.security.CardNumberService;
import com.efmbank.cards.util.PageableSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardBlockRequestService {

    private static final Set<String> BLOCK_REQUEST_SORT_PROPERTIES = Set.of(
        "id",
        "status",
        "createdAt",
        "processedAt"
    );

    private final CardBlockRequestRepository requestRepository;
    private final CardRepository cardRepository;
    private final CardNumberService cardNumberService;

    @Transactional
    public CardBlockRequestResponseDto requestBlock(User user, UUID cardPublicId) {
        Card card = cardRepository.findByPublicIdAndOwnerIdAndDeletedFalse(cardPublicId, user.getId())
            .orElseThrow(CardNotFoundException::new);

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardAlreadyBlockedException();
        }

        if (requestRepository.existsByCardIdAndStatus(card.getId(), BlockRequestStatus.REQUESTED)) {
            throw new BlockRequestAlreadyExistsException();
        }

        CardBlockRequest request = new CardBlockRequest();
        request.setCard(card);
        request.setUser(user);
        request.setStatus(BlockRequestStatus.REQUESTED);

        return toResponse(requestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public Page<CardBlockRequestResponseDto> findByOwner(User user, Pageable pageable) {
        return requestRepository.findAllByUserId(
                user.getId(),
                PageableSanitizer.sanitize(pageable, BLOCK_REQUEST_SORT_PROPERTIES)
            )
            .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CardBlockRequestResponseDto> findRequested(Pageable pageable) {
        return requestRepository.findAllByStatus(
                BlockRequestStatus.REQUESTED,
                PageableSanitizer.sanitize(pageable, BLOCK_REQUEST_SORT_PROPERTIES)
            )
            .map(this::toResponse);
    }

    @Transactional
    public CardBlockRequestResponseDto approve(Long requestId, User admin) {
        CardBlockRequest request = getPendingRequest(requestId);
        request.setStatus(BlockRequestStatus.APPROVED);
        request.setProcessedAt(Instant.now());
        request.setProcessedBy(admin);
        request.getCard().setStatus(CardStatus.BLOCKED);

        return toResponse(request);
    }

    @Transactional
    public CardBlockRequestResponseDto reject(Long requestId, User admin) {
        CardBlockRequest request = getPendingRequest(requestId);
        request.setStatus(BlockRequestStatus.REJECTED);
        request.setProcessedAt(Instant.now());
        request.setProcessedBy(admin);

        return toResponse(request);
    }

    private CardBlockRequest getPendingRequest(Long requestId) {
        CardBlockRequest request = requestRepository.findById(requestId)
            .orElseThrow(BlockRequestNotFoundException::new);

        if (request.getStatus() != BlockRequestStatus.REQUESTED) {
            throw new BlockRequestAlreadyProcessedException();
        }

        return request;
    }

    private CardBlockRequestResponseDto toResponse(CardBlockRequest request) {
        return new CardBlockRequestResponseDto(
            request.getId(),
            request.getCard().getPublicId(),
            cardNumberService.mask(request.getCard().getLastFourDigits()),
            request.getStatus()
        );
    }
}
