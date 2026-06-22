package com.efmbank.cards.controller;

import com.efmbank.cards.dto.CardBlockRequestResponseDto;
import com.efmbank.cards.entity.User;
import com.efmbank.cards.model.BlockRequestStatus;
import com.efmbank.cards.security.AuthService;
import com.efmbank.cards.service.CardBlockRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardBlockRequestController.class)
class CardBlockRequestControllerTest extends ControllerTestSupport {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @MockitoBean
    private CardBlockRequestService requestService;

    @MockitoBean
    private AuthService authService;

    @Test
    void requestBlockShouldReturn401WhenNoJwt() throws Exception {
        mockMvc.perform(post(TestUri.BLOCK_REQUEST_FOR_CARD, UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void requestBlockShouldReturn403WhenRoleIsWrong() throws Exception {
        mockMvc.perform(post(TestUri.BLOCK_REQUEST_FOR_CARD, UUID.randomUUID())
                .with(jwtWithoutRequiredRole()))
            .andExpect(status().isForbidden());
    }

    @Test
    void requestBlockShouldCallService() throws Exception {
        User user = user();
        UUID cardPublicId = UUID.randomUUID();
        CardBlockRequestResponseDto response = response(1L, cardPublicId, BlockRequestStatus.REQUESTED);
        when(authService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(requestService.requestBlock(user, cardPublicId)).thenReturn(response);

        mockMvc.perform(post(TestUri.BLOCK_REQUEST_FOR_CARD, cardPublicId)
                .with(userJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.status").value("REQUESTED"));

        verify(requestService).requestBlock(user, cardPublicId);
    }

    @Test
    void findOwnShouldReturnOnlyForUserRole() throws Exception {
        User user = user();
        CardBlockRequestResponseDto response = response(1L, UUID.randomUUID(), BlockRequestStatus.REQUESTED);
        when(authService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(requestService.findByOwner(any(User.class), any())).thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get(TestUri.BLOCK_REQUEST_MY)
                .with(userJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    void findOwnShouldReturn403ForAdmin() throws Exception {
        mockMvc.perform(get(TestUri.BLOCK_REQUEST_MY)
                .with(adminJwt()))
            .andExpect(status().isForbidden());
    }

    @Test
    void findRequestedShouldReturn403ForUser() throws Exception {
        mockMvc.perform(get(TestUri.BLOCK_REQUEST_REQUESTED)
                .with(userJwt()))
            .andExpect(status().isForbidden());
    }

    @Test
    void findRequestedShouldReturnRequestsForAdmin() throws Exception {
        CardBlockRequestResponseDto response = response(1L, UUID.randomUUID(), BlockRequestStatus.REQUESTED);
        when(requestService.findRequested(any())).thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get(TestUri.BLOCK_REQUEST_REQUESTED)
                .with(adminJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].status").value("REQUESTED"));
    }

    @Test
    void approveShouldCallServiceForAdmin() throws Exception {
        User admin = admin();
        CardBlockRequestResponseDto response = response(1L, UUID.randomUUID(), BlockRequestStatus.APPROVED);
        when(authService.getCurrentUser(any(Authentication.class))).thenReturn(admin);
        when(requestService.approve(1L, admin)).thenReturn(response);

        mockMvc.perform(patch(TestUri.BLOCK_REQUEST_APPROVE, 1L)
                .with(adminJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(requestService).approve(1L, admin);
    }

    @Test
    void rejectShouldCallServiceForAdmin() throws Exception {
        User admin = admin();
        CardBlockRequestResponseDto response = response(1L, UUID.randomUUID(), BlockRequestStatus.REJECTED);
        when(authService.getCurrentUser(any(Authentication.class))).thenReturn(admin);
        when(requestService.reject(1L, admin)).thenReturn(response);

        mockMvc.perform(patch(TestUri.BLOCK_REQUEST_REJECT, 1L)
                .with(adminJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(requestService).reject(1L, admin);
    }

    private CardBlockRequestResponseDto response(Long id, UUID cardPublicId, BlockRequestStatus status) {
        return new CardBlockRequestResponseDto(
            id,
            cardPublicId,
            "**** **** **** 1234",
            status
        );
    }
}
