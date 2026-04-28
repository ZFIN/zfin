package org.zfin.zirc.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.presentation.LookupStrings;

@Controller
@RequestMapping("/zirc")
public class ZircDashboardController {

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String viewDashboard(Model model) {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Line Submission Dashboard");
        return "zirc/dashboard";
    }

}
