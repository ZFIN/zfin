package org.zfin.zirc.api;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.zfin.zirc.service.ZircEntityNotFoundException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RFC 7807 Problem Details handler for the ZIRC JSON API. Scoped to
 * {@code org.zfin.zirc.api} so existing controllers keep their current
 * error behavior; other features can opt in as they are ported.
 *
 * <p>Ordered ahead of {@code ZfinGlobalExceptionHandler}, which has a
 * catch-all {@code @ExceptionHandler(Exception.class)} that would otherwise
 * render an HTML error page for our REST endpoints.
 */
@RestControllerAdvice(basePackages = "org.zfin.zirc.api")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ZircApiExceptionHandler {

    @ExceptionHandler(ZircEntityNotFoundException.class)
    public ProblemDetail handleNotFound(ZircEntityNotFoundException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        pd.setTitle("Not Found");
        pd.setType(URI.create("https://zfin.org/problems/not-found"));
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        pd.setTitle("Bad Request");
        pd.setType(URI.create("https://zfin.org/problems/bad-request"));
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, "Validation failed");
        pd.setTitle("Validation Failed");
        pd.setType(URI.create("https://zfin.org/problems/validation-failed"));
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        pd.setProperty("errors", errors);
        return pd;
    }
}
