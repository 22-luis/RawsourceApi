package com.example.rawsource.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;
import java.net.URI;

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
    
    @Bean
    @Primary
    @ConditionalOnProperty(name = "DATABASE_URL")
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            try {
                URI uri = new URI(databaseUrl);
                String username = uri.getUserInfo().split(":")[0];
                String password = uri.getUserInfo().split(":")[1];
                String host = uri.getHost();
                int port = uri.getPort();
                String database = uri.getPath().substring(1); // Remove leading slash
                
                String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
                
                logger.info("=== Parsed Railway DATABASE_URL ===");
                logger.info("Host: {}", host);
                logger.info("Port: {}", port);
                logger.info("Database: {}", database);
                logger.info("Username: {}", username);
                logger.info("JDBC URL: {}", jdbcUrl);
                logger.info("=====================================");
                
                HikariDataSource dataSource = new HikariDataSource();
                dataSource.setJdbcUrl(jdbcUrl);
                dataSource.setUsername(username);
                dataSource.setPassword(password);
                dataSource.setDriverClassName("org.postgresql.Driver");
                dataSource.setMaximumPoolSize(10);
                dataSource.setMinimumIdle(5);
                dataSource.setConnectionTimeout(30000);
                dataSource.setIdleTimeout(600000);
                dataSource.setMaxLifetime(1800000);
                
                return dataSource;
            } catch (Exception e) {
                logger.error("Error parsing DATABASE_URL: {}", e.getMessage());
                throw new RuntimeException("Failed to parse DATABASE_URL", e);
            }
        }
        
        // Fallback to default configuration
        return null;
    }
} 