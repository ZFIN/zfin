package org.zfin.framework.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller that obtains the meta data for the database.
 */
public class ApplicationContextController extends AbstractCommandController {

    public ApplicationContextController() {
        setCommandClass(ApplicationContextBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {

        ApplicationContextBean form = (ApplicationContextBean) command;
        WebApplicationContext context = RequestContextUtils.getWebApplicationContext(request);
        form.setApplicationContext(context);

        return new ModelAndView("application-context-info", LookupStrings.FORM_BEAN, form);
    }
}
