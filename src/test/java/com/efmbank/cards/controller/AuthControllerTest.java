package com.efmbank.cards.controller;

import com.efmbank.cards.dto.LoginRequestDto;
import com.efmbank.cards.dto.LoginResponseDto;
import com.efmbank.cards.exception.GlobalExceptionHandler;
import com.efmbank.cards.security.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private final AuthService authService = mock(AuthService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
            .standaloneSetup(new AuthController(authService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .build();
    }

    @Test
    void loginShouldReturnJwtToken() throws Exception {
        LoginRequestDto request = new LoginRequestDto("admin", "admin");
        when(authService.login(request)).thenReturn(new LoginResponseDto("jwt-token"));

        mockMvc.perform(post(TestUri.AUTH_LOGIN)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of(
                    "login", "admin",
                    "password", "admin"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jwtToken").value("jwt-token"));

        verify(authService).login(request);
    }

    @Test
    void loginShouldValidateRequestBody() throws Exception {
        mockMvc.perform(post(TestUri.AUTH_LOGIN)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of("login", "admin"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Validation failed"));
    }
}
