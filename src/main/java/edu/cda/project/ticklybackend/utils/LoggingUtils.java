package edu.cda.project.ticklybackend.utils;

import org.slf4j.Logger;
import org.slf4j.MDC;

/**
 * Utility class for standardized logging across the application.
 * Provides methods for common logging patterns and ensures consistent formatting.
 */
public class LoggingUtils {

    private static final String USER_ID_KEY = "userId";
    private static final String REQUEST_ID_KEY = "requestId";
    
    /**
     * Sets the user ID in the Mapped Diagnostic Context (MDC).
     * This allows including the user ID in all subsequent log messages.
     *
     * @param userId the ID of the current user
     */
    public static void setUserId(Long userId) {
        if (userId != null) {
            MDC.put(USER_ID_KEY, userId.toString());
        }
    }
    
    /**
     * Sets the request ID in the Mapped Diagnostic Context (MDC).
     * This allows tracking a request across multiple log messages.
     *
     * @param requestId the ID of the current request
     */
    public static void setRequestId(String requestId) {
        if (requestId != null && !requestId.isEmpty()) {
            MDC.put(REQUEST_ID_KEY, requestId);
        }
    }
    
    /**
     * Clears the MDC context.
     * Should be called at the end of request processing.
     */
    public static void clearContext() {
        MDC.clear();
    }
    
    /**
     * Logs a method entry with parameters.
     *
     * @param logger the logger to use
     * @param methodName the name of the method being entered
     * @param params the parameters to log (name-value pairs)
     */
    public static void logMethodEntry(Logger logger, String methodName, Object... params) {
        if (logger.isDebugEnabled()) {
            StringBuilder message = new StringBuilder("Entering ");
            message.append(methodName).append("(");
            
            for (int i = 0; i < params.length; i += 2) {
                if (i > 0) {
                    message.append(", ");
                }
                message.append(params[i]).append("=");
                
                // Handle sensitive data
                if (isSensitiveParam(params[i].toString())) {
                    message.append("*****");
                } else if (i + 1 < params.length) {
                    message.append(params[i + 1]);
                }
            }
            
            message.append(")");
            logger.debug(message.toString());
        }
    }
    
    /**
     * Logs a method exit with result.
     *
     * @param logger the logger to use
     * @param methodName the name of the method being exited
     * @param result the result to log
     */
    public static void logMethodExit(Logger logger, String methodName, Object result) {
        if (logger.isDebugEnabled()) {
            logger.debug("Exiting {} with result: {}", methodName, result);
        }
    }
    
    /**
     * Logs a method exit without result.
     *
     * @param logger the logger to use
     * @param methodName the name of the method being exited
     */
    public static void logMethodExit(Logger logger, String methodName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Exiting {}", methodName);
        }
    }
    
    /**
     * Logs an exception with context information.
     *
     * @param logger the logger to use
     * @param message the message to log
     * @param ex the exception to log
     */
    public static void logException(Logger logger, String message, Throwable ex) {
        logger.error("{}: {} - {}", message, ex.getClass().getSimpleName(), ex.getMessage(), ex);
    }
    
    /**
     * Checks if a parameter name indicates sensitive data that should be masked.
     *
     * @param paramName the name of the parameter
     * @return true if the parameter contains sensitive data, false otherwise
     */
    private static boolean isSensitiveParam(String paramName) {
        return paramName != null && (
                paramName.toLowerCase().contains("password") ||
                paramName.toLowerCase().contains("token") ||
                paramName.toLowerCase().contains("secret") ||
                paramName.toLowerCase().contains("key") ||
                paramName.toLowerCase().contains("credential")
        );
    }
}