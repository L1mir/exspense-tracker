package org.limir.expensetracker.common;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.limir.expensetracker.rate.ExchangeRateUnavailableException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Turns all errors into the single ProblemDetail format (RFC 9457).
 * The parent class already does it for standard Spring MVC errors (unreadable JSON, unknown URL, ...);
 * here validation errors get a list of invalid fields and our own exceptions get their status.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /** Invalid request body: {@code "errors": {"account_from": "must be 10 digits"}}. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(toSnakeCase(error.getField()), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(validationProblem(errors));
    }

    /**
     * Thrown instead of the above when the method also validates a path variable (e.g. the account number):
     * then Spring validates all parameters together, and body field errors come nested in {@link ParameterErrors}.
     */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
                                                                            HttpHeaders headers,
                                                                            HttpStatusCode status,
                                                                            WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ParameterValidationResult result : ex.getParameterValidationResults()) {
            if (result instanceof ParameterErrors bodyErrors) {
                bodyErrors.getFieldErrors()
                        .forEach(error -> errors.put(toSnakeCase(error.getField()), error.getDefaultMessage()));
            } else {
                String parameter = result.getMethodParameter().getParameterName();
                result.getResolvableErrors().forEach(error -> errors.put(parameter, error.getDefaultMessage()));
            }
        }
        return ResponseEntity.badRequest().body(validationProblem(errors));
    }

    /** No exchange rate: the transaction is not saved, the sender should retry later. */
    @ExceptionHandler(ExchangeRateUnavailableException.class)
    public ProblemDetail handleRateUnavailable(ExchangeRateUnavailableException ex) {
        log.warn("Transaction rejected: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        problem.setTitle("Exchange rate unavailable");
        return problem;
    }

    /** Anything unexpected: log the details, do not expose them to the caller. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
    }

    private static ProblemDetail validationProblem(Map<String, String> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
        problem.setTitle("Invalid request");
        problem.setProperty("errors", errors);
        return problem;
    }

    /** accountFrom -> account_from, to match the JSON field names. */
    private static String toSnakeCase(String fieldName) {
        return fieldName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
