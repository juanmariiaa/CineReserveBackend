package org.example.backend.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        logger.error("Unauthorized error: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        int statusCode;
        String errorType;
        if (authException.getMessage().contains("Bad credentials")) {
            statusCode = HttpServletResponse.SC_UNAUTHORIZED;
            errorType = "Invalid credentials";
        } else if (authException.getMessage().contains("Full authentication is required")) {
            statusCode = HttpServletResponse.SC_FORBIDDEN;
            errorType = "Authentication required";
        } else {
            statusCode = HttpServletResponse.SC_UNAUTHORIZED;
            errorType = "Unauthorized";
        }

        response.setStatus(statusCode);

        final Map<String, Object> body = new HashMap<>();
        body.put("status", statusCode);
        body.put("error", errorType);
        body.put("message", authException.getMessage());
        body.put("path", request.getServletPath());
        body.put("timestamp", System.currentTimeMillis());

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}