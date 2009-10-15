package org.zfin.framework.presentation;

import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

/**
 * Liberablle borrowed from the Confluence Plugin Job Manager, JobManagerAction class.
 */
public class QuartzJobsController extends AbstractCommandController {

    private Scheduler scheduler ;

    protected ModelAndView handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, BindException e) throws Exception {
        ModelAndView modelAndView = new ModelAndView("quartz-jobs.page") ;

        QuartzJobsCommandBean quartzJobsCommandBean = (QuartzJobsCommandBean) o ;
        modelAndView.addObject(LookupStrings.FORM_BEAN, quartzJobsCommandBean) ;

        if(StringUtils.isNotEmpty(quartzJobsCommandBean.getAction()) ){
            String job = quartzJobsCommandBean.getJob() ;
            String group = quartzJobsCommandBean.getGroup() ;
            if(group==null){
                group = "DEFAULT" ;
            }
            if(quartzJobsCommandBean.getAction().equals("run")){
                if(job!=null){
                    logger.info("running job: " + job);
                    scheduler.triggerJob(job,group);
                }
            }else
            if(quartzJobsCommandBean.getAction().equals("pause")){
                if(job!=null){
                    logger.info("pausing job: " + job);
                    scheduler.pauseJob(job,group);
                }
            }
            if(quartzJobsCommandBean.getAction().equals("resume")){
                if(job!=null){
                    logger.info("resuming job: " + job);
                    scheduler.resumeJob(job,group);
                }
            }

            quartzJobsCommandBean.clearLastAction() ;
        }


        // first we get all the jobs
//        String[] groupNames = scheduler.getTriggerGroupNames();
//        for(String triggerGroup: groupNames){
//            String[] triggerNames = scheduler.getTriggerNames(triggerGroup) ;
//            for(String triggerName: triggerNames){
//                Trigger trigger = scheduler.getTrigger(triggerName,triggerGroup) ;
//                QuartzJobInfo quartzJobInfo = new QuartzJobInfo(trigger.getJobName(),trigger.getPreviousFireTime(),trigger.getNextFireTime());
//                quartzJobInfos.add(quartzJobInfo) ;
//            }
//        }

        List<QuartzJobInfo> jobs = new ArrayList<QuartzJobInfo>();
        String[] groups = scheduler.getJobGroupNames();
        for (int x=0; x<groups.length; x++)
        {
            String[] jobNames = scheduler.getJobNames(groups[x]);
            for (int y=0; y<jobNames.length; y++)
            {
                Trigger[] triggers = scheduler.getTriggersOfJob(jobNames[y], groups[x]);
                boolean paused = false;
                Date nextExecution = null;
                Date lastExecution = null;

                // We only need the first trigger to determine if paused
                if (triggers != null && triggers.length > 0)
                {
                    paused = scheduler.getTriggerState(triggers[0].getName(), triggers[0].getGroup()) == Trigger.STATE_PAUSED;
                    for (int t=0; t<triggers.length; t++)
                    {
                        nextExecution = triggers[t].getNextFireTime();
                        lastExecution = triggers[t].getPreviousFireTime();
                        if (nextExecution != null)
                            break;
                    }
                }

                jobs.add(new QuartzJobInfo(jobNames[y], lastExecution, nextExecution, paused));
            }
        }
        quartzJobsCommandBean.setQuartzJobInfoList(jobs);

        return modelAndView ;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
