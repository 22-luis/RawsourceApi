package com.example.rawsource.controllers;

import com.example.rawsource.entities.dto.user.AuthRequest;
import com.example.rawsource.entities.dto.user.AuthResponse;
import com.example.rawsource.entities.dto.user.LogoutRequest;
import com.example.rawsource.entities.dto.user.UserDto;
import com.example.rawsource.services.JwtService;
import com.example.rawsource.services.TokenRevocationService;
import com.example.rawsource.services.UserService;
import com.example.rawsource.utils.SecureLogger;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenRevocationService tokenRevocationService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);
            UserDto userDTO = userService.getUserByEmail(request.getEmail());

            return ResponseEntity.ok(new AuthResponse(token, userDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid credentials: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUYER', 'PROVIDER')")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest logoutRequest) {
        if (logoutRequest.getToken() != null) {
            tokenRevocationService.revokeToken(logoutRequest.getToken());
            SecureLogger.info("User logged out successfully");
            return ResponseEntity.ok().body("Logged out successfully");
        }

        return ResponseEntity.badRequest().body("Token is required");
    }
}
