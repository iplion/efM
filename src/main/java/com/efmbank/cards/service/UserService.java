package com.efmbank.cards.service;

import com.efmbank.cards.dto.UserCreateRequestDto;
import com.efmbank.cards.dto.UserResponseDto;
import com.efmbank.cards.dto.UserUpdateRequestDto;
import com.efmbank.cards.entity.Role;
import com.efmbank.cards.entity.User;
import com.efmbank.cards.exception.DatabaseException;
import com.efmbank.cards.exception.UserAlreadyExistException;
import com.efmbank.cards.exception.UserNotFoundException;
import com.efmbank.cards.model.UserRole;
import com.efmbank.cards.repository.RoleRepository;
import com.efmbank.cards.repository.UserRepository;
import com.efmbank.cards.util.PageableSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Set<String> USER_SORT_PROPERTIES = Set.of(
        "id",
        "login",
        "fullName",
        "enabled",
        "createdAt",
        "updatedAt"
    );

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDto create(UserCreateRequestDto request) {
        if (userRepository.existsByLogin(request.login())) {
            throw new UserAlreadyExistException();
        }

        User user = new User();
        user.setLogin(request.login());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole(getRole(request.role()));
        user.setEnabled(request.enabled());

        try {
            return toResponse(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistException();
        }
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDto> findAll(Pageable pageable) {
        return userRepository.findAll(PageableSanitizer.sanitize(pageable, USER_SORT_PROPERTIES))
            .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponseDto get(Long id) {
        return toResponse(getUser(id));
    }

    @Transactional
    public UserResponseDto update(Long id, UserUpdateRequestDto request) {
        User user = getUser(id);
        user.setFullName(request.fullName());
        user.setRole(getRole(request.role()));
        user.setEnabled(request.enabled());

        return toResponse(user);
    }

    @Transactional
    public UserResponseDto enable(Long id) {
        User user = getUser(id);
        user.setEnabled(true);
        return toResponse(user);
    }

    @Transactional
    public UserResponseDto disable(Long id) {
        User user = getUser(id);
        user.setEnabled(false);
        return toResponse(user);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(UserNotFoundException::new);
    }

    private Role getRole(UserRole role) {
        return roleRepository.findByName(role)
            .orElseThrow(() -> new DatabaseException("Role is not configured: " + role.name()));
    }

    private UserResponseDto toResponse(User user) {
        return new UserResponseDto(
            user.getId(),
            user.getLogin(),
            user.getFullName(),
            user.getRole().getName(),
            user.isEnabled()
        );
    }
}
