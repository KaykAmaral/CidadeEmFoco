package br.com.cidadeemfoco.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRule(
            BusinessRuleException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.UNAUTHORIZED, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleFileTooLarge(
            MaxUploadSizeExceededException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONTENT_TOO_LARGE, "A imagem deve ter no maximo 5 MB", request, Map.of());
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ApiErrorResponse> handleStorage(
            StorageException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );
        return build(HttpStatus.BAD_REQUEST, "Existem campos invalidos", request, fieldErrors);
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestHeaderException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(
            Exception exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, "Requisicao invalida", request, Map.of());
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(status).body(response);
    }
}
