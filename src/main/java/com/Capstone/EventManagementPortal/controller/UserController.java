package com.Capstone.EventManagementPortal.controller;

import com.Capstone.EventManagementPortal.dto.JwtResponse;
import com.Capstone.EventManagementPortal.dto.LoginRequest;
import com.Capstone.EventManagementPortal.dto.UserDTO;
import com.Capstone.EventManagementPortal.model.Role;
import com.Capstone.EventManagementPortal.model.User;
import com.Capstone.EventManagementPortal.security.jwt.JwtUtil;
import com.Capstone.EventManagementPortal.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(AuthenticationManager authenticationManager, UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    // 1️⃣ Register a new user
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody User user) {
        User registeredUser = userService.registerUser(user);
        return ResponseEntity.ok(new UserDTO(registeredUser));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        try {
            System.out.println("🚀 Attempting login with email: " + request.getEmail());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            System.out.println("✅ Authentication successful for: " + request.getEmail());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User registeredUser = userService.findByEmail(request.getEmail());
            Role userRole = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(Role::valueOf)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("❌ Role extraction failed"));

            String token = jwtUtil.generateToken(request.getEmail(), userRole);
            return ResponseEntity.ok(new JwtResponse(token));
        } catch (Exception e) {
            System.out.println("❌ Login failed: " + e.getMessage()); // ✅ Capture error
            e.printStackTrace();  // ✅ Print stack trace
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }



    // 2️⃣ Get all users (ADMIN Only)
    @GetMapping("/")
    public ResponseEntity<List<UserDTO>> getAllUsers(Authentication authentication) throws AccessDeniedException {
        jwtUtil.checkAdminAccess(authentication);

        List<UserDTO> userDTOs = userService.getAllUsers().stream()
                .map(user -> new UserDTO(user)) // ✅ Correctly mapping User -> UserDTO
                .toList();

        return ResponseEntity.ok(userDTOs);
    }

    // 3️⃣ Get user by ID (Authenticated Users)
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
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
