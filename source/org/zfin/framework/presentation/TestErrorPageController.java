package org.zfin.framework.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class TestErrorPageController extends SimpleFormController {

    @Override
    protected ModelAndView onSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, BindException e) throws Exception {
        String errortype = httpServletRequest.getParameter("error-type");
        if (true) {
            throw new RuntimeException("tests that the page got an error");
        }

        return new ModelAndView("no-error.page");
    }

}
