package com.Capstone.EventManagementPortal.service;

import com.Capstone.EventManagementPortal.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(User user);
    Optional<User> getUserById(Long id);
    List<User> getAllUsers();
    Optional<User> getUserByEmail(String email);
    User updateUser(Long id, User userDetails, String loggedInEmail);  // Added method
    void deleteUser(Long id, String loggedInEmail);  // Added method

}
