package com.efmbank.cards.repository;

import com.efmbank.cards.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    @EntityGraph(attributePaths = {"owner", "sourceCard", "targetCard"})
    Optional<Transfer> findByPublicId(UUID publicId);

    Page<Transfer> findAllByOwnerId(Long ownerId, Pageable pageable);
}
