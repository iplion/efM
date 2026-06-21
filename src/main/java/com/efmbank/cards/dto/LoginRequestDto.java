package com.efmbank.cards.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
    @NotBlank
    @Size(max = 30)
    String login,

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank
    @Size(max = 150)
    String password

) {
    @Override
    public String toString() {
        return "LoginRequestDto[login=%s, password=***]".formatted(login);
    }

}
