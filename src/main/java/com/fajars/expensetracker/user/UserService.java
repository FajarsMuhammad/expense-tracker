package com.fajars.expensetracker.user;

import com.fajars.expensetracker.user.UserDto;
import com.fajars.expensetracker.user.User;
import com.fajars.expensetracker.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto getByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserDto(user.getId(), user.getEmail(), user.getName());
    }
}
