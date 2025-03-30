package com.Capstone.EventManagementPortal.controller;

import com.Capstone.EventManagementPortal.dto.UserDTO;
import com.Capstone.EventManagementPortal.model.User;
import com.Capstone.EventManagementPortal.security.jwt.JwtUtil;
import com.Capstone.EventManagementPortal.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // 1️⃣ Register a new user
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody User user) {
        User registeredUser = userService.registerUser(user);
        return ResponseEntity.ok(new UserDTO(registeredUser));
    }

    // 2️⃣ Get all users (ADMIN Only)
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(Authentication authentication) {
        jwtUtil.checkAdminAccess(authentication);
        return ResponseEntity.ok(userService.getAllUsers().stream()
                .map(UserDTO::new).toList());
    }

    // 3️⃣ Get user by ID (Authenticated Users)
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        return ResponseEntity.ok(new UserDTO(user));
    }

    // 4️⃣ Get user by Email (Authenticated Users)
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        return ResponseEntity.ok(new UserDTO(user));
    }

    // 5️⃣ Update user details (User can update only their own account)
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody User userDetails, Authentication authentication) {
        String loggedInEmail = authentication.getName();
        User updatedUser = userService.updateUser(id, userDetails, loggedInEmail);
        return ResponseEntity.ok(new UserDTO(updatedUser));
    }

    // 6️⃣ Delete user (Only Admin or the User Themselves)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id, Authentication authentication) {
        String loggedInEmail = authentication.getName();
        userService.deleteUser(id, loggedInEmail);
        return ResponseEntity.ok("User deleted successfully!");
    }
}
