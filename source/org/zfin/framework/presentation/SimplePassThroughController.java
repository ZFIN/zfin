package org.zfin.framework.presentation;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.throwaway.ThrowawayController;

/**
 * This Controller just takes the tiles view name and returns that ModelAndView object
 * in case you do not have any logic to perform in the Controller other than
 * passing through to the view handler.
 */
public class SimplePassThroughController implements ThrowawayController {

    private String viewName;

    public ModelAndView execute() throws Exception {
        return new ModelAndView(viewName);
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
}