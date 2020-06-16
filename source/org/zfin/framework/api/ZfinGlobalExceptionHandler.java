package org.zfin.framework.api;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.zfin.framework.presentation.ErrorResource;
import org.zfin.framework.presentation.FieldErrorResource;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.presentation.PageLayoutControllerAdvice;
import org.zfin.marker.MarkerNotFoundException;
import org.zfin.profile.service.ProfileService;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@Log4j2
public class ZfinGlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String customHandleGeneralException(Exception exception, WebRequest request, Model model) {
        //RestErrorMessage error = restErrorException.getError();
        if (exception.getMessage() != null) {
            model.addAttribute("error", exception.getMessage());
            log.error(exception.getMessage(), exception);
        } else {
            log.error("General Error", exception);
        }
        if (ProfileService.isRootUser()) {
            model.addAttribute("exception", exception);
            model.addAttribute("stackTrace", ExceptionUtils.getFullStackTrace(exception));
        }
        model.addAttribute("pageURL", ((ServletWebRequest) request).getRequest().getRequestURI());
        advice.populateModelAttributes(model);
        return "infrastructure/exception.page";
    }

    @ExceptionHandler(RestErrorException.class)
    public ResponseEntity<RestErrorMessage> customHandleNotFound(RestErrorException restErrorException, WebRequest request) {
        RestErrorMessage error = restErrorException.getError();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MarkerNotFoundException.class)
    public String handleMarkerNotFoundException(MarkerNotFoundException e, HttpServletResponse response, Model model) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        model.addAttribute(LookupStrings.ZDB_ID, e.getZdbID());
        model.addAttribute("copyrightYear", advice.populateCopyrightYear());
        advice.populateModelAttributes(model);
        return LookupStrings.RECORD_NOT_FOUND_PAGE;
    }

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler({InvalidWebRequestException.class})
    protected ResponseEntity<Object> handleInvalidRequest(InvalidWebRequestException ire, WebRequest request) {

        List<FieldErrorResource> fieldErrorResources = new ArrayList<>();


        if (ire.getErrors() != null) {
            List<FieldError> fieldErrors = ire.getErrors().getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                FieldErrorResource fieldErrorResource = new FieldErrorResource();
                fieldErrorResource.setResource(fieldError.getObjectName());
                fieldErrorResource.setField(fieldError.getField());
                fieldErrorResource.setCode(fieldError.getCode());
                fieldErrorResource.setMessage(messageSource.getMessage(fieldError, null));
                fieldErrorResources.add(fieldErrorResource);
            }

            for (ObjectError globalError : ire.getErrors().getGlobalErrors()) {
                FieldErrorResource fieldErrorResource = new FieldErrorResource();
                fieldErrorResource.setResource(globalError.getObjectName());
                fieldErrorResource.setField("$global");
                fieldErrorResource.setCode(globalError.getCode());
                fieldErrorResource.setMessage(messageSource.getMessage(globalError, null));
                fieldErrorResources.add(fieldErrorResource);
            }
        }

        ErrorResource error = new ErrorResource("InvalidRequest", ire.getMessage());
        error.setFieldErrors(fieldErrorResources);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return handleExceptionInternal(ire, error, headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    @Autowired
    private PageLayoutControllerAdvice advice;

}