package org.zfin.framework.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class TestErrorPageController {

    private final static transient Logger LOGGER = Logger.getLogger(TestErrorPageController.class);

    @RequestMapping(value = "/test-error-page", method = RequestMethod.POST)
    protected String testErrors() throws Exception {
        final RuntimeException runtimeException = new RuntimeException("tests that the page got an error");
        LOGGER.error("A test exception: First logging", runtimeException);
        LOGGER.error("A test exception: Second logging");
        LOGGER.error("A test exception: Third logging");
        throw runtimeException;
    }

    @RequestMapping(value = "/test-error-page", method = RequestMethod.GET)
    protected String testErrorsStart() throws Exception {
        return "dev-tools/create-error.page";
    }

}
