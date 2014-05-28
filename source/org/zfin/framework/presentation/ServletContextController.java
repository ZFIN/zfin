package org.zfin.framework.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Controller that obtains the meta data for the database.
 */
@Controller
public class ServletContextController {

    @RequestMapping("/servlet-context")
    protected String showServletContext(@ModelAttribute("formBean") ServletInfoBean form,
                                        HttpServletRequest request,
                                        Model model) throws Exception {

        form.setContext(request.getSession().getServletContext());
        return "servlet-context-info";
    }
}
