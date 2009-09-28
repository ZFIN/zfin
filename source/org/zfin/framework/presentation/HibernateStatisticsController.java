package org.zfin.framework.presentation;

import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.zfin.framework.HibernateUtil;
import org.hibernate.stat.Statistics;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller that obtains the meta data for the database.
 */
public class HibernateStatisticsController extends AbstractCommandController {

    public HibernateStatisticsController(){
        setCommandClass(HibernateStatisticsBean.class);
    }
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        HibernateStatisticsBean form = (HibernateStatisticsBean) command;

        Statistics stats =  HibernateUtil.getSessionFactory().getStatistics();

        String reset =  request.getParameter("reset") ;
        if(reset!=null){
            if(reset.equals("true")){
                stats.clear();
                return new ModelAndView("redirect:/action/dev-tools/view-hibernate-statistics");
            }
        }

        stats.setStatisticsEnabled(true);
        form.setStatistics(stats);
        return new ModelAndView("hibernate-statistics-view", LookupStrings.FORM_BEAN, form);
    }
}
