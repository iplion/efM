package com.efmbank.cards.controller;

import com.efmbank.cards.dto.UserCreateRequestDto;
import com.efmbank.cards.dto.UserResponseDto;
import com.efmbank.cards.dto.UserUpdateRequestDto;
import com.efmbank.cards.model.UserRole;
import com.efmbank.cards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
class AdminUserControllerTest extends ControllerTestSupport {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void createShouldReturn403WhenRoleIsNotAdmin() throws Exception {
        mockMvc.perform(post(TestUri.ADMIN_USERS)
                .with(userJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateRequestJson())))
            .andExpect(status().isForbidden());
    }

    @Test
    void createShouldReturn400WhenRequestInvalid() throws Exception {
        UserCreateRequestDto request = new UserCreateRequestDto("", "password", "", UserRole.USER, true);

        mockMvc.perform(post(TestUri.ADMIN_USERS)
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createShouldReturnUserAndCallService() throws Exception {
        UserCreateRequestDto request = userCreateRequest();
        UserResponseDto response = userResponse(1L, UserRole.USER, true);
        when(userService.create(request)).thenReturn(response);

        mockMvc.perform(post(TestUri.ADMIN_USERS)
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateRequestJson())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).create(request);
    }

    @Test
    void findAllShouldReturnUsers() throws Exception {
        when(userService.findAll(any())).thenReturn(new PageImpl<>(List.of(userResponse(1L, UserRole.USER, true))));

        mockMvc.perform(get(TestUri.ADMIN_USERS)
                .with(adminJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].login").value("user"));
    }

    @Test
    void getShouldReturnUser() throws Exception {
        when(userService.get(1L)).thenReturn(userResponse(1L, UserRole.USER, true));

        mockMvc.perform(get(TestUri.ADMIN_USER_BY_ID, 1L)
                .with(adminJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateShouldCallService() throws Exception {
        UserUpdateRequestDto request = new UserUpdateRequestDto("Updated User", UserRole.ADMIN, true);
        when(userService.update(1L, request)).thenReturn(userResponse(1L, UserRole.ADMIN, true));

        mockMvc.perform(put(TestUri.ADMIN_USER_BY_ID, 1L)
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(userService).update(1L, request);
    }

    @Test
    void enableShouldCallService() throws Exception {
        when(userService.enable(1L)).thenReturn(userResponse(1L, UserRole.USER, true));

        mockMvc.perform(patch(TestUri.ADMIN_USER_ENABLE, 1L)
                .with(adminJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void disableShouldCallService() throws Exception {
        when(userService.disable(1L)).thenReturn(userResponse(1L, UserRole.USER, false));

        mockMvc.perform(patch(TestUri.ADMIN_USER_DISABLE, 1L)
                .with(adminJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enabled").value(false));
    }

    // helpers ------------------------

    private UserCreateRequestDto userCreateRequest() {
        return new UserCreateRequestDto("user", "password", "Test User", UserRole.USER, true);
    }

    private Map<String, Object> userCreateRequestJson() {
        return Map.of(
            "login", "user",
            "password", "password",
            "fullName", "Test User",
            "role", UserRole.USER,
            "enabled", true
        );
    }

    private UserResponseDto userResponse(Long id, UserRole role, boolean enabled) {
        return new UserResponseDto(id, "user", "Test User", role, enabled);
    }

}
