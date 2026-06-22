package com.efmbank.cards.controller;

import com.efmbank.cards.dto.TransferCreateRequestDto;
import com.efmbank.cards.dto.TransferResponseDto;
import com.efmbank.cards.entity.User;
import com.efmbank.cards.model.TransferStatus;
import com.efmbank.cards.security.AuthService;
import com.efmbank.cards.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
class TransferControllerTest extends ControllerTestSupport {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransferService transferService;

    @MockitoBean
    private AuthService authService;

    @Test
    void transferShouldReturn401WhenNoJwt() throws Exception {
        mockMvc.perform(post(TestUri.TRANSFERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferCreateRequest())))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void transferShouldReturn403WhenRoleIsWrong() throws Exception {
        mockMvc.perform(post(TestUri.TRANSFERS)
                .with(jwtWithoutRequiredRole())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferCreateRequest())))
            .andExpect(status().isForbidden());
    }

    @Test
    void transferShouldReturn400WhenAmountInvalid() throws Exception {
        mockMvc.perform(post(TestUri.TRANSFERS)
                .with(userJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "publicId", UUID.randomUUID(),
                    "sourceCardId", UUID.randomUUID(),
                    "targetCardId", UUID.randomUUID(),
                    "amount", "0.00"
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void transferShouldReturnResponseAndCallService() throws Exception {
        User user = user();
        TransferCreateRequestDto request = transferCreateRequest();
        TransferResponseDto response = transferResponse(request.publicId());
        when(authService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(transferService.transfer(user, request)).thenReturn(response);

        mockMvc.perform(post(TestUri.TRANSFERS)
                .with(userJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transferPublicId").value(request.publicId().toString()))
            .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(transferService).transfer(user, request);
    }

    @Test
    void findByOwnerShouldReturnTransfers() throws Exception {
        User user = user();
        TransferResponseDto response = transferResponse(UUID.randomUUID());
        when(authService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(transferService.findByOwner(any(User.class), any())).thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get(TestUri.TRANSFERS)
                .with(userJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].transferPublicId").value(response.transferPublicId().toString()));
    }

    private TransferCreateRequestDto transferCreateRequest() {
        return new TransferCreateRequestDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            new BigDecimal("10.00")
        );
    }

    private TransferResponseDto transferResponse(UUID publicId) {
        return new TransferResponseDto(
            publicId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            new BigDecimal("10.00"),
            TransferStatus.COMPLETED
        );
    }
}
