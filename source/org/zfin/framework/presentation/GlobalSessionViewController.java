package org.zfin.framework.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class GlobalSessionViewController extends AbstractCommandController{

    /**
     * This is here only for the view global sesssion.  Could be moved into another controller, however.
     */
    private GlobalSessionBean globalSessionBean;

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        globalSessionBean.setCurrentSession(request.getSession(false)) ;
        command = globalSessionBean;
        return new ModelAndView("view-global-session-info", LookupStrings.FORM_BEAN, command);
    }


    public GlobalSessionBean getZfinGlobalSessionBean() {
        return globalSessionBean;
    }

    public void setZfinGlobalSessionBean(GlobalSessionBean globalSessionBean) {
        this.globalSessionBean = globalSessionBean;
    }

}
