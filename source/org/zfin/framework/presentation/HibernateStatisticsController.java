package org.zfin.framework.presentation;

import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.HibernateUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller that obtains the meta data for the database.
 */
@Controller
public class HibernateStatisticsController {

    @RequestMapping("/view-hibernate-statistics")
    protected String showBrowserInfo(@ModelAttribute("formBean") HibernateStatisticsBean form) throws Exception {

        Statistics stats = HibernateUtil.getSessionFactory().getStatistics();
        stats.setStatisticsEnabled(true);
        form.setStatistics(stats);
        return "hibernate-statistics-view.page";
    }

    @RequestMapping("/view-hibernate-statistics/reset")
    protected String resetHibernateStats() throws Exception {

        Statistics stats = HibernateUtil.getSessionFactory().getStatistics();
        stats.clear();
        return "redirect:/action/devtool/view-hibernate-statistics";
    }
}
