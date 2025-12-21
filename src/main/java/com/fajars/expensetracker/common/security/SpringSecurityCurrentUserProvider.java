package com.fajars.expensetracker.common.security;

import com.fajars.expensetracker.auth.AuthenticatedUser;
import com.fajars.expensetracker.common.exception.UnauthorizedException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public UUID getUserId() {
        try {
            AuthenticatedUser user = getAuthenticatedUser();
            return user.getUserId();
        } catch (Exception e) {
            return UUID.fromString("00000000-0000-0000-0000-000000000000");
        }
    }


    @Override
    public String getEmail() {
        try {
            AuthenticatedUser user = getAuthenticatedUser();
            return user.getEmail();
        } catch (Exception e) {
            return "system";
        }

    }

    private AuthenticatedUser getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder
            .getContext()
            .getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new UnauthorizedException("Unauthenticated");
        }
        return user;
    }
}
