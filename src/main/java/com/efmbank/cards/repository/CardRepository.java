package com.efmbank.cards.repository;

import com.efmbank.cards.entity.Card;
import com.efmbank.cards.model.CardStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, Long> {

    @EntityGraph(attributePaths = "owner")
    Optional<Card> findByPublicIdAndDeletedFalse(UUID publicId);

    @EntityGraph(attributePaths = "owner")
    Optional<Card> findByPublicIdAndOwnerIdAndDeletedFalse(UUID publicId, Long ownerId);

    boolean existsByCardNumberHash(String cardNumberHash);

    @Query("""
        select c from Card c
        join fetch c.owner
        where c.publicId in :publicIds
          and c.owner.id = :ownerId
          and c.deleted = false
        """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Card> findOwnedCardsForUpdate(
        @Param("publicIds") List<UUID> publicIds,
        @Param("ownerId") Long ownerId
    );

    @Query("""
        select c from Card c
        join c.owner o
        where (:ownerId is null or o.id = :ownerId)
          and (:status is null or c.status = :status)
          and c.deleted = false
    """)
    Page<Card> findAllCards(
        @Param("ownerId") Long ownerId,
        @Param("status") CardStatus status,
        Pageable pageable
    );
}
