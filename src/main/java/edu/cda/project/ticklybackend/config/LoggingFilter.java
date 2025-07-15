package edu.cda.project.ticklybackend.config;

import edu.cda.project.ticklybackend.utils.LoggingUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that sets up MDC context for each request.
 * This ensures that all log messages within a request have the same context information.
 */
@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Generate a unique request ID
            String requestId = UUID.randomUUID().toString();
            LoggingUtils.setRequestId(requestId);

            // Add the request ID to the response headers for debugging
            response.addHeader("X-Request-ID", requestId);

            // Set the user ID in the MDC if a user is authenticated
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                    !"anonymousUser".equals(authentication.getPrincipal().toString())) {
                try {
                    // Try to get the user ID from the principal
                    Object principal = authentication.getPrincipal();
                    if (principal instanceof edu.cda.project.ticklybackend.models.user.User) {
                        Long userId = ((edu.cda.project.ticklybackend.models.user.User) principal).getId();
                        LoggingUtils.setUserId(userId);
                    }
                } catch (Exception e) {
                    // Log the exception but continue processing the request
                    logger.warn("Error setting user ID in MDC: " + e.getMessage());
                }
            }

            // Continue with the filter chain
            filterChain.doFilter(request, response);
        } finally {
            // Clear the MDC context after the request is processed
            LoggingUtils.clearContext();
        }
    }
}
