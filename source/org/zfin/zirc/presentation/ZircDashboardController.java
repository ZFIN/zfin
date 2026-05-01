package org.zfin.zirc.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.zirc.entity.LineSubmission;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/zirc")
public class ZircDashboardController {

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String viewDashboard(Model model) {
        List<LineSubmission> submissions = HibernateUtil.currentSession()
                .createQuery("from ZircLineSubmission order by createdAt desc", LineSubmission.class)
                .list();
        // No status column on LineSubmission yet, so everything lands in "Active".
        model.addAttribute("activeSubmissions", submissions);
        model.addAttribute("closedSubmissions", Collections.emptyList());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Line Submission Dashboard");
        return "zirc/dashboard";
    }

}
