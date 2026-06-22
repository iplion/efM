package com.efmbank.cards.repository;

import com.efmbank.cards.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "role")
    Optional<User> findByLogin(String login);

    boolean existsByLogin(String login);
}
