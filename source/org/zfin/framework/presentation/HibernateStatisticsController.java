package org.zfin.framework.presentation;

import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.HibernateUtil;

/**
 * Controller that obtains the meta data for the database.
 */
@Controller
@RequestMapping(value = "/devtool")
public class HibernateStatisticsController {

    @RequestMapping("/view-hibernate-statistics")
    protected String showBrowserInfo(@ModelAttribute("formBean") HibernateStatisticsBean form) throws Exception {

        Statistics stats = HibernateUtil.getSessionFactory().getStatistics();
        stats.setStatisticsEnabled(true);
        form.setStatistics(stats);
        return "dev-tools/hibernate-statistics-view";
    }

    @RequestMapping("/view-hibernate-statistics/reset")
    protected String resetHibernateStats() throws Exception {

        Statistics stats = HibernateUtil.getSessionFactory().getStatistics();
        stats.clear();
        return "redirect:/action/devtool/view-hibernate-statistics";
    }
}
