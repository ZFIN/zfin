package org.zfin.framework.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ZfinGlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(RestErrorException.class)
    public ResponseEntity<RestErrorMessage> customHandleNotFound(RestErrorException restErrorException, WebRequest request) {
        RestErrorMessage error = restErrorException.getError();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

}