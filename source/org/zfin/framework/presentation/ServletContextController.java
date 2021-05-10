package org.zfin.framework.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Controller that obtains the meta data for the database.
 */
@Controller
@RequestMapping(value = "/devtool")
public class ServletContextController {

    @RequestMapping("/servlet-context")
    protected String showServletContext(@ModelAttribute("formBean") ServletInfoBean form,
                                        HttpServletRequest request) throws Exception {

        form.setContext(request.getSession().getServletContext());
        return "dev-tools/servlet-context-info";
    }
}
