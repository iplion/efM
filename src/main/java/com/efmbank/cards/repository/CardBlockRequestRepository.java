package com.efmbank.cards.repository;

import com.efmbank.cards.entity.CardBlockRequest;
import com.efmbank.cards.model.BlockRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardBlockRequestRepository extends JpaRepository<CardBlockRequest, Long> {

    boolean existsByCardIdAndStatus(Long cardId, BlockRequestStatus status);

    Page<CardBlockRequest> findAllByUserId(Long userId, Pageable pageable);

    Page<CardBlockRequest> findAllByStatus(BlockRequestStatus status, Pageable pageable);
}
