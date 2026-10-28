package ru.practicum.exception;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class AdviceController extends ResponseEntityExceptionHandler {

    private static final String REASON_BAD_REQUEST = "Incorrectly made request.";
    private static final String REASON_NOT_FOUND = "The required object was not found.";
    private static final String REASON_INTEGRITY = "Integrity constraint has been violated.";
    private static final String REASON_CONDITIONS = "For the requested operation the conditions are not met.";
    private static final String REASON_INTERNAL = "Unexpected error.";

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException e) {
        log.warn("Object not found: {}", e.getMessage());
        return response(HttpStatus.NOT_FOUND, REASON_NOT_FOUND, e.getMessage(), List.of());
    }

    @ExceptionHandler(ValidateException.class)
    public ResponseEntity<ApiError> handleValidate(ValidateException e) {
        log.warn("Conditions are not met: {}", e.getMessage());
        return response(HttpStatus.CONFLICT, REASON_CONDITIONS, e.getMessage(), List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException e) {
        String message = e.getMessage() != null ? e.getMessage() : e.getMostSpecificCause().getMessage();
        log.warn("Integrity constraint violated: {}", message);
        return response(HttpStatus.CONFLICT, REASON_INTEGRITY, message, List.of());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException e) {
        List<String> errors = e.getConstraintViolations().stream()
                .map(violation -> fieldMessage(fieldName(violation.getPropertyPath().toString()),
                        violation.getMessage(),
                        violation.getInvalidValue()))
                .toList();
        return badRequest(errors, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Incorrect request: {}", e.getMessage());
        return response(HttpStatus.BAD_REQUEST, REASON_BAD_REQUEST, e.getMessage(), List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        return response(HttpStatus.INTERNAL_SERVER_ERROR, REASON_INTERNAL, message, List.of());
    }

    @Override
    protected ResponseEntity<Object> handleMethodValidationException(MethodValidationException e,
                                                                     HttpHeaders headers,
                                                                     HttpStatus status,
                                                                     WebRequest request) {
        HttpStatus resolvedStatus = e.isForReturnValue() ? status : HttpStatus.BAD_REQUEST;
        return handleExceptionInternal(e, null, headers, resolvedStatus, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception e,
                                                             Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {
        if (isCommitted(request)) {
            log.warn("Response already committed: {}", e.getMessage());
            return null;
        }

        HttpStatus status = resolveStatus(statusCode);
        List<String> errors = validationErrors(e);
        String message = messageOf(errors, readableMessage(e));
        if (status.is5xxServerError()) {
            log.error("Request failed", e);
        } else {
            log.warn("Request failed: {}", message);
        }
        return new ResponseEntity<>(apiError(status, reasonFor(status), message, errors), headers, status);
    }

    private ResponseEntity<ApiError> badRequest(List<String> errors, String fallback) {
        String message = messageOf(errors, fallback);
        log.warn("Validation failed: {}", message);
        return response(HttpStatus.BAD_REQUEST, REASON_BAD_REQUEST, message, errors);
    }

    private boolean isCommitted(WebRequest request) {
        if (request instanceof ServletWebRequest servletRequest) {
            HttpServletResponse response = servletRequest.getResponse();
            return response != null && response.isCommitted();
        }
        return false;
    }

    private HttpStatus resolveStatus(HttpStatusCode statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        return status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String reasonFor(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> REASON_BAD_REQUEST;
            case NOT_FOUND -> REASON_NOT_FOUND;
            case CONFLICT -> REASON_CONDITIONS;
            case INTERNAL_SERVER_ERROR -> REASON_INTERNAL;
            default -> status.getReasonPhrase();
        };
    }

    private List<String> validationErrors(Exception e) {
        if (e instanceof BindException bindException) {
            List<String> errors = new ArrayList<>();
            bindException.getFieldErrors().stream()
                    .map(error -> fieldMessage(error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
                    .forEach(errors::add);
            bindException.getGlobalErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .filter(Objects::nonNull)
                    .forEach(errors::add);
            return errors;
        }
        if (e instanceof MethodValidationResult validationResult) {
            return methodValidationErrors(validationResult);
        }
        return List.of();
    }

    private List<String> methodValidationErrors(MethodValidationResult validationResult) {
        List<String> errors = new ArrayList<>();
        for (ParameterValidationResult result : validationResult.getAllValidationResults()) {
            if (result instanceof ParameterErrors parameterErrors) {
                parameterErrors.getFieldErrors().stream()
                        .map(error -> fieldMessage(error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
                        .forEach(errors::add);
                continue;
            }
            String field = result.getMethodParameter().getParameterName();
            for (MessageSourceResolvable resolvable : result.getResolvableErrors()) {
                errors.add(fieldMessage(field != null ? field : "parameter",
                        resolvable.getDefaultMessage(),
                        result.getArgument()));
            }
        }
        return errors;
    }

    private String readableMessage(Exception e) {
        if (e instanceof HttpMessageNotReadableException notReadable) {
            String causeMessage = notReadable.getMostSpecificCause().getMessage();
            if (causeMessage != null) {
                return causeMessage;
            }
        }
        if (e.getMessage() != null) {
            return e.getMessage();
        }
        return e.getClass().getSimpleName();
    }

    private String messageOf(List<String> errors, String fallback) {
        if (!errors.isEmpty()) {
            return String.join("; ", errors);
        }
        return fallback != null ? fallback : "Unexpected error";
    }

    private String fieldMessage(String field, String error, Object value) {
        return String.format("Field: %s. Error: %s. Value: %s", field, error, value);
    }

    private String fieldName(String path) {
        int separator = path.lastIndexOf('.');
        return separator >= 0 ? path.substring(separator + 1) : path;
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String reason, String message, List<String> errors) {
        return ResponseEntity.status(status).body(apiError(status, reason, message, errors));
    }

    private ApiError apiError(HttpStatus status, String reason, String message, List<String> errors) {
        ApiError apiError = new ApiError();
        apiError.setStatus(status.name());
        apiError.setReason(reason);
        apiError.setMessage(message);
        apiError.setTimestamp(LocalDateTime.now());
        if (!errors.isEmpty()) {
            apiError.setErrors(new ArrayList<>(errors));
        }
        return apiError;
    }
}
