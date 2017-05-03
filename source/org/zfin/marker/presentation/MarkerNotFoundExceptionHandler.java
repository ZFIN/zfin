package org.zfin.marker.presentation;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.MarkerNotFoundException;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice(basePackageClasses = MarkerNotFoundExceptionHandler.class)
public class MarkerNotFoundExceptionHandler {
    @ExceptionHandler(MarkerNotFoundException.class)
    public String handleMarkerNotFoundException (MarkerNotFoundException e, HttpServletResponse response, Model model) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        model.addAttribute(LookupStrings.ZDB_ID, e.getZdbID());
        return LookupStrings.RECORD_NOT_FOUND_PAGE;
    }
}
