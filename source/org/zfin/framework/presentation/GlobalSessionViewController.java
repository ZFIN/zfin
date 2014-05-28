package org.zfin.framework.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class GlobalSessionViewController {

    @Autowired
    private SessionRegistry sessionRegistry;

    @RequestMapping("/view-global-session")
    protected String showGlobalSession(@ModelAttribute("formBean") GlobalSessionBean form,
                                       HttpServletRequest servletRequest) throws Exception {
        form.setCurrentSession(servletRequest.getSession(false));
        form.setSessionRegistry(sessionRegistry);
        return "view-global-session-info.page";
    }
}
