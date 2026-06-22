package com.efmbank.cards.repository;

import com.efmbank.cards.entity.Role;
import com.efmbank.cards.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(UserRole name);
}
