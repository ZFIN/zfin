package org.zfin.infrastructure.presentation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@RequestMapping("/")
public class ErrorController extends ResponseEntityExceptionHandler {

    private Logger logger = LogManager.getLogger(ErrorController.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleAllExceptions(Exception ex, Model model) {

        logger.error("Exception caught by ErrorController",ex);

        return "infrastructure/exception";

    }

    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public String throwAnError() throws Exception {
        throw new NullPointerException();
    }


}
