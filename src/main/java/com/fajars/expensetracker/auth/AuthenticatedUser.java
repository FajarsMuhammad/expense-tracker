package com.fajars.expensetracker.auth;

import com.fajars.expensetracker.user.domain.User;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Value
@Builder
public class AuthenticatedUser implements UserDetails {

    UUID userId;
    String email;
    String name;
    String password;
    Collection<? extends GrantedAuthority> authorities;

    public static AuthenticatedUser valueOf(User user) {
        return AuthenticatedUser.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .password(user.getPasswordHash())
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
            .build();
    }

    @Override
    public String getUsername() {
        return email;
    }
}
