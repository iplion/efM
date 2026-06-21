package com.efmbank.cards.security;

import com.efmbank.cards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BankUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) {
        return userRepository.findByLogin(login)
            .map(BankUserDetails::new)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
