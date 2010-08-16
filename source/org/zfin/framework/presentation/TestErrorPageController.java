package org.zfin.framework.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class TestErrorPageController extends SimpleFormController {

    private final static transient Logger LOGGER = Logger.getLogger(TestErrorPageController.class);

    @Override
    protected ModelAndView onSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, BindException e) throws Exception {
        String errortype = httpServletRequest.getParameter("error-type");
        if (true) {
            final RuntimeException runtimeException = new RuntimeException("tests that the page got an error");
            LOGGER.error("A test exception: First logging", runtimeException);
            LOGGER.error("A test exception: Second logging");
            LOGGER.error("A test exception: Third logging");
            throw runtimeException;
        }

        return new ModelAndView("no-error.page");
    }

}
