package com.Capstone.EventManagementPortal.service.impl;

import com.Capstone.EventManagementPortal.dto.UserDTO;
import com.Capstone.EventManagementPortal.exception.EmailAlreadyExistsException;
import com.Capstone.EventManagementPortal.exception.UserNotFoundException;
import com.Capstone.EventManagementPortal.model.User;
import com.Capstone.EventManagementPortal.repository.UserRepository;
import com.Capstone.EventManagementPortal.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.Capstone.EventManagementPortal.model.Role;

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
    @Transactional
    public User registerUser(User user) {
        // Check if email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email is already in use: " + user.getEmail());
        }

        // Check if username already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken: " + user.getUsername());
        }

        // Validate Role
        if (user.getRole() == null) {
            throw new RuntimeException("Role cannot be null!");
        }

        // Validate Password
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password cannot be empty!");
        }

        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));


        return userRepository.save(user);
    }




    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));
    }


    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();  // Returns List<User>
    }



    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));
    }

    @Override
    public User updateUser(Long id, User userDetails, String loggedInEmail) {
        User loggedInUser = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (loggedInUser.getRole() == Role.ADMIN || loggedInUser.getId().equals(id)) {
            if (userDetails.getUsername() != null && !userDetails.getUsername().trim().isEmpty()) {
                user.setUsername(userDetails.getUsername());
            }
            if (userDetails.getEmail() != null && !userDetails.getEmail().trim().isEmpty()) {
                user.setEmail(userDetails.getEmail());
            }
            if (userDetails.getRole() != null) {
                // ✅ Fix: Remove "ROLE_" prefix if it exists before saving
                String roleName = userDetails.getRole().name().replace("ROLE_", "");
                user.setRole(Role.valueOf(roleName));
            }
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }

            User updatedUser = userRepository.save(user);
            System.out.println("✅ User updated successfully: " + updatedUser);
            return updatedUser;
        }

        throw new RuntimeException("Unauthorized to update user");
    }


    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }


    @Override
    public void deleteUser(Long id, String loggedInEmail) {
        User loggedInUser = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));

        if (loggedInUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("Unauthorized to delete user");
        } else if (loggedInUser.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admins cannot delete themselves.");
        }

        userRepository.deleteById(id);
    }
}
