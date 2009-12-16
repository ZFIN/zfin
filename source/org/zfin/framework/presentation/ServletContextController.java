package org.zfin.framework.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller that obtains the meta data for the database.
 */
public class ServletContextController extends AbstractCommandController {

    public ServletContextController() {
        setCommandClass(ServletInfoBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {

        ServletInfoBean form = (ServletInfoBean) command;
        form.setContext(request.getSession().getServletContext());
        return new ModelAndView("serlvet-context-info", LookupStrings.FORM_BEAN, form);
    }
}
