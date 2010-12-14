package org.zfin.people.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Allows editing the allowable use of prefixes. Using AbstactCommandcontroller to allow
 * continual editing.
 */
public class EditLabPrefixController extends AbstractCommandController{

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
