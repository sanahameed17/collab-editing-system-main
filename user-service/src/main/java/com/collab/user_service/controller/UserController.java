package com.collab.user_service.controller;

import com.collab.user_service.model.User;
import com.collab.user_service.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "APIs for user registration, authentication, and profile management")
public class UserController {

    private final UserRepository repo;

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account with username, email, and password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully",
                content = @Content(schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "409", description = "User with this email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // Check if user already exists
        if (repo.findByEmail(user.getEmail()).isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User with this email already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        
        User saved = repo.save(user);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("user", saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "User authentication", description = "Authenticates a user with email and password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        User found = repo.findByEmail(user.getEmail()).orElse(null);
        Map<String, Object> response = new HashMap<>();
        
        if (found != null && found.getPassword().equals(user.getPassword())) {
            response.put("message", "Login successful");
            response.put("userId", found.getId());
            response.put("username", found.getUsername());
            response.put("email", found.getEmail());
            return ResponseEntity.ok(response);
        }
        
        response.put("message", "Invalid credentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @Operation(summary = "Get user by ID", description = "Retrieves user information by user ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found",
                content = @Content(schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id) {
        return repo.findById(id)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all users", description = "Retrieves a list of all registered users")
    @ApiResponse(responseCode = "200", description = "List of users retrieved successfully")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(repo.findAll());
    }

    @Operation(summary = "Update user profile", description = "Updates user profile information (username, email, password)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(
            @Parameter(description = "User ID", required = true) @PathVariable Long id,
            @RequestBody User updatedUser) {
        return repo.findById(id).map(u -> {
            if (updatedUser.getUsername() != null) {
                u.setUsername(updatedUser.getUsername());
            }
            if (updatedUser.getEmail() != null) {
                // Check if email is already taken by another user
                repo.findByEmail(updatedUser.getEmail()).ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new RuntimeException("Email already in use");
                    }
                });
                u.setEmail(updatedUser.getEmail());
            }
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                u.setPassword(updatedUser.getPassword());
            }
            User saved = repo.save(u);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("user", saved);
            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete user", description = "Deletes a user account by user ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }
}
