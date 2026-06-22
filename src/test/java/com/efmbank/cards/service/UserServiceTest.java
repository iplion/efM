package com.efmbank.cards.service;

import com.efmbank.cards.dto.UserCreateRequestDto;
import com.efmbank.cards.entity.Role;
import com.efmbank.cards.entity.User;
import com.efmbank.cards.exception.DatabaseException;
import com.efmbank.cards.exception.UserAlreadyExistException;
import com.efmbank.cards.model.UserRole;
import com.efmbank.cards.repository.RoleRepository;
import com.efmbank.cards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createShouldEncodePasswordAndSaveUser() {
        UserCreateRequestDto request = new UserCreateRequestDto(
            "user",
            "password",
            "Test User",
            UserRole.USER,
            true
        );
        Role role = role(UserRole.USER);

        when(userRepository.existsByLogin(request.login())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(roleRepository.findByName(UserRole.USER)).thenReturn(Optional.of(role));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        var response = userService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.login()).isEqualTo("user");
        assertThat(response.role()).isEqualTo(UserRole.USER);
        assertThat(response.enabled()).isTrue();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded-password");
        assertThat(userCaptor.getValue().getRole()).isSameAs(role);
    }

    @Test
    void createShouldRejectExistingLogin() {
        UserCreateRequestDto request = request();
        when(userRepository.existsByLogin(request.login())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
            .isInstanceOf(UserAlreadyExistException.class);

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void createShouldFailWhenRoleIsNotConfigured() {
        UserCreateRequestDto request = request();

        when(userRepository.existsByLogin(request.login())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(roleRepository.findByName(UserRole.USER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.create(request))
            .isInstanceOf(DatabaseException.class)
            .hasMessageContaining("Role is not configured");
    }

    @Test
    void createShouldTranslateUniqueViolationToUserAlreadyExists() {
        UserCreateRequestDto request = request();

        when(userRepository.existsByLogin(request.login())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(roleRepository.findByName(UserRole.USER)).thenReturn(Optional.of(role(UserRole.USER)));
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> userService.create(request))
            .isInstanceOf(UserAlreadyExistException.class);
    }

    private UserCreateRequestDto request() {
        return new UserCreateRequestDto(
            "user",
            "password",
            "Test User",
            UserRole.USER,
            true
        );
    }

    private Role role(UserRole name) {
        Role role = new Role();
        role.setId(1);
        role.setName(name);
        return role;
    }
}
