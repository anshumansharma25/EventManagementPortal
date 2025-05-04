package com.Capstone.EventManagementPortal.service;

import com.Capstone.EventManagementPortal.dto.UserDTO;
import com.Capstone.EventManagementPortal.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(User user);
    User getUserById(Long userId);
    List<User> getAllUsers();
//    Optional<User> getUserByEmail(String email);
    User updateUser(Long id, User userDetails, String loggedInEmail);  // Added method
    void deleteUser(Long id, String loggedInEmail);  // Added method
    User findByEmail(String email);  // ✅ Define method in service
    User getUserByEmail(String email);

}
