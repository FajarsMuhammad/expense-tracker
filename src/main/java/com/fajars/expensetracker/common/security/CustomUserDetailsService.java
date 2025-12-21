package com.fajars.expensetracker.common.security;

import com.fajars.expensetracker.auth.AuthenticatedUser;
import com.fajars.expensetracker.user.domain.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
            .map(AuthenticatedUser::valueOf)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
