package com.example.rawsource.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RateLimitFilter implements Filter {

    @Autowired
    @Qualifier("loginBucket")
    private Bucket loginBucket;

    @Autowired
    @Qualifier("generalBucket")
    private Bucket generalBucket;

    @Autowired
    @Qualifier("criticalBucket")
    private Bucket criticalBucket;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String clientIp = getClientIpAddress(httpRequest);
        String requestUri = httpRequest.getRequestURI();
        
        Bucket bucket = selectBucket(requestUri);
        
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
        }
    }

    private Bucket selectBucket(String uri) {
        if (uri.contains("/api/auth/login")) {
            return loginBucket;
        } else if (uri.contains("/api/users/register") || uri.contains("/api/products")) {
            return criticalBucket;
        } else {
            return generalBucket;
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
