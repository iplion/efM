package com.efmbank.cards.controller;

import com.efmbank.cards.config.SecurityConfig;
import com.efmbank.cards.entity.User;
import com.efmbank.cards.model.UserRole;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@Import({
    SecurityConfig.class
})
abstract class ControllerTestSupport {

    @MockitoBean
    JwtDecoder jwtDecoder;

    protected JwtRequestPostProcessor userJwt() {
        return jwt()
            .jwt(jwt -> jwt
                .subject("user")
                .claim("role", UserRole.USER.name())
            )
            .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    protected JwtRequestPostProcessor adminJwt() {
        return jwt()
            .jwt(jwt -> jwt
                .subject("admin")
                .claim("role", UserRole.ADMIN.name())
            )
            .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    protected JwtRequestPostProcessor jwtWithoutRequiredRole() {
        return jwt()
            .jwt(jwt -> jwt
                .subject("guest")
                .claim("role", "GUEST")
            )
            .authorities(new SimpleGrantedAuthority("ROLE_GUEST"));
    }

    protected User user() {
        User user = new User();
        user.setId(1L);
        user.setLogin("user");
        return user;
    }

    protected User admin() {
        User admin = new User();
        admin.setId(2L);
        admin.setLogin("admin");
        return admin;
    }
}
