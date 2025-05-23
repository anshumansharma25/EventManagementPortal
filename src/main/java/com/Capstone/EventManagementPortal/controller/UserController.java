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

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody User user) {
        User registeredUser = userService.registerUser(user);
        return ResponseEntity.ok(new UserDTO(registeredUser));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User registeredUser = userService.findByEmail(request.getEmail());
            Role userRole = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(Role::valueOf)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Role extraction failed"));

            String token = jwtUtil.generateToken(request.getEmail(), userRole);
            return ResponseEntity.ok(new JwtResponse(token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
//        User user = userService.getUserById(id);
//        return ResponseEntity.ok(new UserDTO(user));
//    }
//
//    @GetMapping("/email/{email}")
//    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
//        User user = userService.getUserByEmail(email);
//        return ResponseEntity.ok(new UserDTO(user));
//    }
}
