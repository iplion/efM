package com.efmbank.cards.controller;

import com.efmbank.cards.dto.CardCreateRequestDto;
import com.efmbank.cards.dto.CardResponseDto;
import com.efmbank.cards.dto.CardUpdateRequestDto;
import com.efmbank.cards.model.CardStatus;
import com.efmbank.cards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCardController.class)
class AdminCardControllerTest extends ControllerTestSupport {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardService cardService;

    @Test
    void createShouldReturn403WhenRoleIsNotAdmin() throws Exception {
        mockMvc.perform(post(TestUri.ADMIN_CARDS)
                .with(userJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardCreateRequest())))
            .andExpect(status().isForbidden());
    }

    @Test
    void createShouldReturn401WhenNoJwt() throws Exception {
        mockMvc.perform(post(TestUri.ADMIN_CARDS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardCreateRequest())))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void createShouldReturn400WhenRequestInvalid() throws Exception {
        CardCreateRequestDto request = new CardCreateRequestDto(
            1L,
            "123",
            LocalDate.now().minusDays(1),
            new BigDecimal("10.00")
        );

        mockMvc.perform(post(TestUri.ADMIN_CARDS)
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createShouldReturnCardAndCallService() throws Exception {
        CardCreateRequestDto request = cardCreateRequest();
        CardResponseDto response = cardResponse(UUID.randomUUID(), CardStatus.ACTIVE);
        when(cardService.create(request)).thenReturn(response);

        mockMvc.perform(post(TestUri.ADMIN_CARDS)
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.publicId").value(response.publicId().toString()))
            .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(cardService).create(request);
    }

    @Test
    void findAllShouldPassFiltersToService() throws Exception {
        CardResponseDto response = cardResponse(UUID.randomUUID(), CardStatus.ACTIVE);
        when(cardService.findAll(eq(1L), eq(CardStatus.ACTIVE), any()))
            .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get(TestUri.ADMIN_CARDS)
                .with(adminJwt())
                .param("ownerId", "1")
                .param("status", "ACTIVE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].publicId").value(response.publicId().toString()));

        verify(cardService).findAll(eq(1L), eq(CardStatus.ACTIVE), any());
    }

    @Test
    void getShouldReturnCard() throws Exception {
        UUID publicId = UUID.randomUUID();
        when(cardService.get(publicId)).thenReturn(cardResponse(publicId, CardStatus.ACTIVE));

        mockMvc.perform(get(TestUri.ADMIN_CARD_BY_PUBLIC_ID, publicId)
                .with(adminJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.publicId").value(publicId.toString()));
    }

    @Test
    void updateShouldCallService() throws Exception {
        UUID publicId = UUID.randomUUID();
        CardUpdateRequestDto request = new CardUpdateRequestDto(
            LocalDate.now().plusYears(1),
            CardStatus.BLOCKED,
            new BigDecimal("50.00")
        );
        when(cardService.update(publicId, request)).thenReturn(cardResponse(publicId, CardStatus.BLOCKED));

        mockMvc.perform(put(TestUri.ADMIN_CARD_BY_PUBLIC_ID, publicId)
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("BLOCKED"));

        verify(cardService).update(publicId, request);
    }

    @Test
    void activateShouldCallService() throws Exception {
        UUID publicId = UUID.randomUUID();
        when(cardService.activate(publicId)).thenReturn(cardResponse(publicId, CardStatus.ACTIVE));

        mockMvc.perform(patch(TestUri.ADMIN_CARD_ACTIVATE, publicId)
                .with(adminJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void blockShouldCallService() throws Exception {
        UUID publicId = UUID.randomUUID();
        when(cardService.block(publicId)).thenReturn(cardResponse(publicId, CardStatus.BLOCKED));

        mockMvc.perform(patch(TestUri.ADMIN_CARD_BLOCK, publicId)
                .with(adminJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void deleteShouldCallService() throws Exception {
        UUID publicId = UUID.randomUUID();

        mockMvc.perform(delete(TestUri.ADMIN_CARD_BY_PUBLIC_ID, publicId)
                .with(adminJwt()))
            .andExpect(status().isOk());

        verify(cardService).delete(publicId);
    }

    private CardCreateRequestDto cardCreateRequest() {
        return new CardCreateRequestDto(
            1L,
            "4111111111111234",
            LocalDate.now().plusYears(3),
            new BigDecimal("100.00")
        );
    }

    private CardResponseDto cardResponse(UUID publicId, CardStatus status) {
        return new CardResponseDto(
            publicId,
            1L,
            "user",
            "**** **** **** 1234",
            LocalDate.now().plusYears(3),
            status,
            new BigDecimal("100.00")
        );
    }
}
