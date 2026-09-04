package com.sivamachineworks.platform.shared.exception;

import com.sivamachineworks.platform.shared.dto.ErrorDetail;
import com.sivamachineworks.platform.shared.dto.ErrorResponse;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private String getCorrelationId() {
        String id = MDC.get("correlationId");
        return id != null ? id : UUID.randomUUID().toString();
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, ErrorCode code, String message, List<ErrorDetail> details) {
        ErrorResponse.Error error = new ErrorResponse.Error(code.name(), message, details);
        ErrorResponse response = new ErrorResponse(false, Instant.now(), error, getCorrelationId());
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<ErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
            .map(err -> new ErrorDetail(err.getField(), err.getDefaultMessage()))
            .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, "Validation failed", details);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case BAD_REQUEST, VALIDATION_FAILED, BUSINESS_RULE_VIOLATION, STATE_TRANSITION_FAILED -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case CONFLICT, OPTIMISTIC_LOCK_FAILED -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return buildResponse(status, ex.getErrorCode(), ex.getMessage(), null);
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockException(OptimisticLockException ex) {
        return buildResponse(HttpStatus.CONFLICT, ErrorCode.OPTIMISTIC_LOCK_FAILED, "The entity was updated by another user", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, "An unexpected error occurred", null);
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(org.springframework.security.authentication.BadCredentialsException ex) {
        ErrorResponse response = new ErrorResponse(
            false,
            Instant.now(),
            new ErrorResponse.Error("UNAUTHORIZED", "Invalid username or password", null),
            UUID.randomUUID().toString()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    @ExceptionHandler(org.springframework.security.authentication.DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(org.springframework.security.authentication.DisabledException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "User account is disabled", null);
    }
    
    @ExceptionHandler(org.springframework.security.core.userdetails.UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "Invalid username or password", null);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, "Access is denied", null);
    }
    
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(org.springframework.security.core.AuthenticationException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "Authentication failed", null);
    }
}
