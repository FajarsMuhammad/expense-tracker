package com.fajars.expensetracker.user;

import com.fajars.expensetracker.user.api.UserResponse;
import com.fajars.expensetracker.user.domain.User;
import com.fajars.expensetracker.user.domain.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserResponse(user.getId(), user.getEmail(), user.getName());
    }
}
