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
 * Servlet filter that initializes and clears per-request logging context (MDC).
 * <p>
 * For each HTTP request, a unique request ID is generated and exposed via the
 * "X-Request-ID" response header. If the user is authenticated and the principal
 * is the application's User type, the user ID is also stored in the logging context.
 * The context is cleared after the request is processed.
 */
@Component
public class LoggingFilter extends OncePerRequestFilter {

    /**
     * Populates the logging context with a unique request ID and, when available,
     * the authenticated user's ID. The request ID is also added to the response header.
     * The logging context is cleared once the filter chain completes.
     *
     * @param request  the incoming HTTP servlet request
     * @param response the HTTP servlet response
     * @param filterChain the filter chain to continue processing
     * @throws ServletException if an exception occurs during filtering
     * @throws IOException if an I/O error occurs during filtering
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String requestId = UUID.randomUUID().toString();
            LoggingUtils.setRequestId(requestId);

            response.addHeader("X-Request-ID", requestId);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() &&
                    !"anonymousUser".equals(authentication.getPrincipal().toString())) {
                try {
                    Object principal = authentication.getPrincipal();
                    if (principal instanceof edu.cda.project.ticklybackend.models.user.User) {
                        Long userId = ((edu.cda.project.ticklybackend.models.user.User) principal).getId();
                        LoggingUtils.setUserId(userId);
                    }
                } catch (Exception e) {
                    logger.warn("Error setting user ID in MDC: " + e.getMessage());
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            LoggingUtils.clearContext();
        }
    }
}
