package org.zfin.framework.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.throwaway.ThrowawayController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

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