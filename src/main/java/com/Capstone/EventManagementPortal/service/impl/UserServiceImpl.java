package com.Capstone.EventManagementPortal.service.impl;

import com.Capstone.EventManagementPortal.model.Role;
import com.Capstone.EventManagementPortal.model.User;
import com.Capstone.EventManagementPortal.repository.UserRepository;
import com.Capstone.EventManagementPortal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(User user) {
        // Check if email is already registered
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered!");
        }

        // Validate role (Ensuring the provided role is a valid enum)
        try {
            Role.valueOf(user.getRole().name());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role!");
        }

        // Hash password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User updateUser(Long id, User userDetails, String loggedInEmail) {
        User loggedInUser = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Allow update if user is ADMIN or updating their own profile
        if (loggedInUser.getRole() == Role.ADMIN || loggedInUser.getId().equals(id)) {
            user.setUsername(userDetails.getUsername());
            user.setEmail(userDetails.getEmail());

            // Update password if provided
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }

            return userRepository.save(user);
        }
        throw new RuntimeException("Unauthorized to update user");
    }

    @Override
    public void deleteUser(Long id, String loggedInEmail) {
        User loggedInUser = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));

        if (loggedInUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("Unauthorized to delete user");
        }
        else if (loggedInUser.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admins cannot delete themselves.");
        }


        userRepository.deleteById(id);
    }
}
