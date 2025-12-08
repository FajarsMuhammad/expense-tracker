package com.fajars.expensetracker.auth;

import com.fajars.expensetracker.user.User;
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
public class UserIdentity implements UserDetails {

    UUID userId;
    String username;
    String password;
    String name;
    Collection<? extends GrantedAuthority> authorities;

    public static UserIdentity valueOf(User user) {
        return UserIdentity.builder()
            .userId(user.getId())
            .username(user.getEmail())
            .name(user.getName())
            .password(user.getPasswordHash())
            .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
