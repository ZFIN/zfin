package org.zfin.framework.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/devtool")
public class GlobalSessionViewController {

    @Autowired
    private SessionRegistry sessionRegistry;

    @RequestMapping("/view-global-session")
    protected String showGlobalSession(@ModelAttribute("formBean") GlobalSessionBean form,
                                       HttpServletRequest servletRequest) throws Exception {
        form.setCurrentSession(servletRequest.getSession(false));
        form.setSessionRegistry(sessionRegistry);
        return "dev-tools/view-global-session-info";
    }
}
