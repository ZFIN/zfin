package org.zfin.framework.presentation;

import org.springframework.validation.Errors;

@SuppressWarnings("serial")
public class InvalidWebRequestException extends RuntimeException {

    private Errors errors;

    public InvalidWebRequestException(String message) {
        super(message);
    }

    public InvalidWebRequestException(String message, Errors errors) {
        super(message);
        this.errors = errors;
    }

    public Errors getErrors() {
        return errors;
    }
}