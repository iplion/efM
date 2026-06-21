package com.efmbank.cards.repository;

import com.efmbank.cards.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {

}
