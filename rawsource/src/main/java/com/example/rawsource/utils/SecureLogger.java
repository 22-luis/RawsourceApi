package com.example.rawsource.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.rawsource.config.SecureLogFilter;

@Component
public class SecureLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(SecureLogger.class);

    public static void info(String message) {
        logger.info(SecureLogFilter.sanitizeLogMessage(message));
    }

    public static void info(String message, Object... args) {
        String sanitizedMessage = SecureLogFilter.sanitizeLogMessage(message);
        logger.info(sanitizeArgs(sanitizedMessage, args));
    }
    
    public static void warn(String message) {
        logger.warn(SecureLogFilter.sanitizeLogMessage(message));
    }
    
    public static void warn(String message, Object... args) {
        String sanitizedMessage = SecureLogFilter.sanitizeLogMessage(message);
        logger.warn(sanitizeArgs(sanitizedMessage, args));
    }
    
    public static void error(String message, Throwable throwable) {
        logger.error(SecureLogFilter.sanitizeLogMessage(message), throwable);
    }
    
    private static String sanitizeArgs(String message, Object... args) {
        if (args == null || args.length == 0) return message;
        
        Object[] sanitizedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null) {
                sanitizedArgs[i] = SecureLogFilter.sanitizeLogMessage(args[i].toString());
            }
        }
        
        return String.format(message, sanitizedArgs);
    }
}
