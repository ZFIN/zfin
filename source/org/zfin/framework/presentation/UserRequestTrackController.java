package org.zfin.framework.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller that obtains the meta data for the database.
 */
public class UserRequestTrackController extends AbstractCommandController {

    public UserRequestTrackController() {
        setCommandClass(UserRequestTrackBean.class);
        setCommandName(LookupStrings.FORM_BEAN);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        UserRequestTrackBean form = (UserRequestTrackBean) command;
        return new ModelAndView("user-request-tracking", LookupStrings.FORM_BEAN, form);
    }
}
