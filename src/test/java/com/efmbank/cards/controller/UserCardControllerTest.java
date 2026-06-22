package com.efmbank.cards.controller;

import com.efmbank.cards.dto.CardResponseDto;
import com.efmbank.cards.entity.User;
import com.efmbank.cards.model.CardStatus;
import com.efmbank.cards.security.AuthService;
import com.efmbank.cards.service.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserCardController.class)
class UserCardControllerTest extends ControllerTestSupport {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private AuthService authService;

    @Test
    void findAllShouldReturn401WhenNoJwt() throws Exception {
        mockMvc.perform(get(TestUri.USER_CARDS))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void findAllShouldReturn403WhenRoleIsWrong() throws Exception {
        mockMvc.perform(get(TestUri.USER_CARDS)
                .with(jwtWithoutRequiredRole()))
            .andExpect(status().isForbidden());
    }

    @Test
    void findAllShouldReturnOwnCardsWithStatusFilter() throws Exception {
        User user = user();
        CardResponseDto response = cardResponse(UUID.randomUUID());
        when(authService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(cardService.findAll(eq(user.getId()), eq(CardStatus.ACTIVE), any()))
            .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get(TestUri.USER_CARDS)
                .with(userJwt())
                .param("status", "ACTIVE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].publicId").value(response.publicId().toString()));

        verify(cardService).findAll(eq(user.getId()), eq(CardStatus.ACTIVE), any());
    }

    @Test
    void getOwnShouldReturnCard() throws Exception {
        User user = user();
        UUID publicId = UUID.randomUUID();
        when(authService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(cardService.getOwn(publicId, user.getId())).thenReturn(cardResponse(publicId));

        mockMvc.perform(get(TestUri.USER_CARD_BY_PUBLIC_ID, publicId)
                .with(userJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.publicId").value(publicId.toString()));

        verify(cardService).getOwn(publicId, user.getId());
    }

    @Test
    void getBalanceShouldReturnBalance() throws Exception {
        User user = user();
        UUID publicId = UUID.randomUUID();
        when(authService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(cardService.getOwn(publicId, user.getId())).thenReturn(cardResponse(publicId));

        mockMvc.perform(get(TestUri.USER_CARD_BALANCE, publicId)
                .with(userJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cardId").value(publicId.toString()))
            .andExpect(jsonPath("$.balance").value(100.00));
    }

    private CardResponseDto cardResponse(UUID publicId) {
        return new CardResponseDto(
            publicId,
            1L,
            "user",
            "**** **** **** 1234",
            LocalDate.now().plusYears(1),
            CardStatus.ACTIVE,
            new BigDecimal("100.00")
        );
    }
}
