package com.lumi.ai.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleValidationError(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                List<FieldErrorResponse> fieldErrors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> new FieldErrorResponse(
                                                error.getField(),
                                                "body",
                                                error.getDefaultMessage(),
                                                "Verifique as regras de validação do campo"))
                                .toList();

                ApiErrorResponse response = new ApiErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                                ErrorType.VALIDATION_ERROR,
                                "Erro de validação nos dados enviados",
                                "Um ou mais campos estão inválidos",
                                request.getRequestURI(),
                                fieldErrors);

                return ResponseEntity
                                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                                .body(response);
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleNotFound(
                        ResourceNotFoundException ex,
                        HttpServletRequest request) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(new ApiErrorResponse(
                                                LocalDateTime.now(),
                                                HttpStatus.NOT_FOUND.value(),
                                                ErrorType.RESOURCE_NOT_FOUND,
                                                "Recurso não encontrado",
                                                ex.getMessage(),
                                                request.getRequestURI(),
                                                null));
        }

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ApiErrorResponse> handleBadRequest(
                        BadRequestException ex,
                        HttpServletRequest request) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(new ApiErrorResponse(
                                                LocalDateTime.now(),
                                                HttpStatus.BAD_REQUEST.value(),
                                                ErrorType.BAD_REQUEST,
                                                "Requisição inválida",
                                                ex.getMessage(),
                                                request.getRequestURI(),
                                                null));
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiErrorResponse> handleAuthError(
                        BadCredentialsException ex,
                        HttpServletRequest request) {
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(new ApiErrorResponse(
                                                LocalDateTime.now(),
                                                HttpStatus.UNAUTHORIZED.value(),
                                                ErrorType.AUTH_ERROR,
                                                "Falha de autenticação",
                                                "Usuário ou senha inválidos",
                                                request.getRequestURI(),
                                                null));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleGeneric(
                        Exception ex,
                        HttpServletRequest request) {
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new ApiErrorResponse(
                                                LocalDateTime.now(),
                                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                ErrorType.INTERNAL_ERROR,
                                                "Erro interno no servidor",
                                                "Ocorreu um erro inesperado",
                                                request.getRequestURI(),
                                                null));
        }

        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("timestamp", LocalDateTime.now());
                body.put("status", ex.getStatusCode().value());
                body.put("message", ex.getReason());

                return new ResponseEntity<>(body, ex.getStatusCode());
        }
}
