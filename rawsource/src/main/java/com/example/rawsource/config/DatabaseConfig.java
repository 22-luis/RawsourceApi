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
            logger.info("=== Railway Environment Variables ===");
            logger.info("PORT: {}", System.getenv("PORT"));
            logger.info("DATABASE_URL: {}", System.getenv("DATABASE_URL"));
            logger.info("SPRING_DATASOURCE_URL: {}", System.getenv("SPRING_DATASOURCE_URL"));
            logger.info("DB_HOST: {}", System.getenv("DB_HOST"));
            logger.info("DB_PORT: {}", System.getenv("DB_PORT"));
            logger.info("DB_NAME: {}", System.getenv("DB_NAME"));
            logger.info("DB_USERNAME: {}", System.getenv("DB_USERNAME"));
            logger.info("DB_PASSWORD: {}", System.getenv("DB_PASSWORD") != null ? "[SET]" : "[NOT SET]");
            logger.info("SPRING_DATASOURCE_USERNAME: {}", System.getenv("SPRING_DATASOURCE_USERNAME"));
            logger.info("SPRING_DATASOURCE_PASSWORD: {}", System.getenv("SPRING_DATASOURCE_PASSWORD") != null ? "[SET]" : "[NOT SET]");
            logger.info("=====================================");
        };
    }
} 