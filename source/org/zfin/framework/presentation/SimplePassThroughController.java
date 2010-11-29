package org.zfin.framework.presentation;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This Controller just takes the tiles view name and returns that ModelAndView object
 * in case you do not have any logic to perform in the Controller other than
 * passing through to the view handler.
 */
public class SimplePassThroughController implements Controller {

    protected String viewName;

    @Override
    public ModelAndView handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        return new ModelAndView(viewName);
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
}