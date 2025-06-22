package com.example.rawsource.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/")
    public ResponseEntity<String> root() {
        return ResponseEntity.ok("RawSource API is running!");
    }

    @GetMapping("/api/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "API is working correctly");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("port", System.getProperty("server.port", "unknown"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "RawSource API");
        response.put("status", "running");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("java.version", System.getProperty("java.version"));
        response.put("server.port", System.getProperty("server.port"));
        response.put("spring.profiles.active", System.getProperty("spring.profiles.active"));
        return ResponseEntity.ok(response);
    }
} 