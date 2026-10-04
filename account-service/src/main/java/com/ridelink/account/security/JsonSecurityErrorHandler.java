package com.ridelink.account.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.account.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Security errors happen in the filter chain, before controllers, so @RestControllerAdvice
 * cannot see them. This class writes the same JSON error format for 401 and 403.
 */
@Component
public class JsonSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JsonSecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override   // no/invalid token -> 401
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        write(request, response, HttpStatus.UNAUTHORIZED, "Authentication required: missing, invalid or expired token");
    }

    @Override   // valid token but wrong role -> 403
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        write(request, response, HttpStatus.FORBIDDEN, "You do not have permission to perform this action");
    }

    private void write(HttpServletRequest request, HttpServletResponse response,
                       HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ErrorResponse(
                Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI(), null));
    }
}
