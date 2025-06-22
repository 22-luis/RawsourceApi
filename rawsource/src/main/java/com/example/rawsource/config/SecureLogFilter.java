package com.example.rawsource.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class SecureLogFilter {

    private static final List<String> SENSITIVE_FIELDS = Arrays.asList("password", "token", "secret", "key", "credential", "authorization", "credentials");
    
    public static String sanitizeLogMessage(String message) {
        if (message == null) return null;
        
        String sanitized = message;
        for (String field : SENSITIVE_FIELDS) {
            // Patrón para encontrar valores sensibles
            String pattern = "(?i)(" + field + "\\s*[:=]\\s*)([^\\s,}]+)";
            sanitized = sanitized.replaceAll(pattern, "$1***REDACTED***");
        }
        
        // Ocultar JWT tokens
        sanitized = sanitized.replaceAll("Bearer\\s+[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*", "Bearer ***REDACTED***");
        
        // Ocultar emails completos
        sanitized = sanitized.replaceAll("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b", "***EMAIL***");
        
        return sanitized;
    }
    
}
