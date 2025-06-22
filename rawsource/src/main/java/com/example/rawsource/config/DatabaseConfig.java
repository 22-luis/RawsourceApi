package com.example.rawsource.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    
    @Value("${spring.datasource.url}")
    private String datasourceUrl;
    
    @Value("${spring.datasource.username}")
    private String datasourceUsername;
    
    @Value("${server.port}")
    private String serverPort;
    
    @Bean
    public CommandLineRunner logDatabaseConfig() {
        return args -> {
            logger.info("=== Database Configuration ===");
            logger.info("Server Port: {}", serverPort);
            logger.info("Database URL: {}", datasourceUrl);
            logger.info("Database Username: {}", datasourceUsername);
            logger.info("Database Password: [HIDDEN]");
            logger.info("================================");
            
            // Log environment variables for debugging
            logger.info("=== Environment Variables ===");
            logger.info("PORT: {}", System.getenv("PORT"));
            logger.info("DATABASE_URL: {}", System.getenv("DATABASE_URL"));
            logger.info("PGDATABASE_URL: {}", System.getenv("PGDATABASE_URL"));
            logger.info("PGUSER: {}", System.getenv("PGUSER"));
            logger.info("PGPASSWORD: {}", System.getenv("PGPASSWORD") != null ? "[SET]" : "[NOT SET]");
            logger.info("================================");
        };
    }
} 