package org.zfin.framework.api;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.zfin.profile.service.ProfileService;

@ControllerAdvice

public class ZfinGlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String customHandleGeneralException(Exception exception, WebRequest request, Model model) {
        //RestErrorMessage error = restErrorException.getError();
        if (exception.getMessage() != null)
            model.addAttribute("error", exception.getMessage());

        if (ProfileService.isRootUser()) {
            model.addAttribute("exception", exception);
            model.addAttribute("stackTrace", ExceptionUtils.getFullStackTrace(exception));
        }
        return "infrastructure/exception.page";
    }

    @ExceptionHandler(RestErrorException.class)
    public ResponseEntity<RestErrorMessage> customHandleNotFound(RestErrorException restErrorException, WebRequest request) {
        RestErrorMessage error = restErrorException.getError();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

}