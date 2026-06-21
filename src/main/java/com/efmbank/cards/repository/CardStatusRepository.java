package com.efmbank.cards.repository;

import com.efmbank.cards.entity.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardStatusRepository extends JpaRepository<CardStatus, Integer> {

}
