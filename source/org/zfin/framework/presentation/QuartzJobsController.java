package org.zfin.framework.presentation;

import org.apache.commons.lang.StringUtils;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

/**
 * Liberablle borrowed from the Confluence Plugin Job Manager, JobManagerAction class.
 */
public class QuartzJobsController extends AbstractCommandController {

    private Scheduler scheduler;

    protected ModelAndView handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, BindException e) throws Exception {
        ModelAndView modelAndView = new ModelAndView("quartz-jobs.page");

        QuartzJobsCommandBean quartzJobsCommandBean = (QuartzJobsCommandBean) o;
        modelAndView.addObject(LookupStrings.FORM_BEAN, quartzJobsCommandBean);

        if (StringUtils.isNotEmpty(quartzJobsCommandBean.getAction())) {
            String job = quartzJobsCommandBean.getJob();
            String group = quartzJobsCommandBean.getGroup();
            if (group == null) {
                group = "DEFAULT";
            }
            if (quartzJobsCommandBean.getAction().equals("run")) {
                if (job != null) {
                    logger.info("running job: " + job);
                    scheduler.triggerJob(job, group);
                }
            }
            else
            if (quartzJobsCommandBean.getAction().equals("pause")) {
                if (job != null) {
                    logger.info("pausing job: " + job);
                    scheduler.pauseJob(job, group);
                }
            }
            else
            if (quartzJobsCommandBean.getAction().equals("resume")) {
                if (job != null) {
                    logger.info("resuming job: " + job);
                    scheduler.resumeJob(job, group);
                }
            }
            else
            if (quartzJobsCommandBean.getAction().equals("pauseAll")) {
                logger.info("pausing all jobs" );
                scheduler.pauseAll();
            }
            else
            if (quartzJobsCommandBean.getAction().equals("resumeAll")) {
                logger.info("resuming all jobs");
                if(false==scheduler.isStarted()){
                    scheduler.start();
                }
                scheduler.resumeAll();
            }

            quartzJobsCommandBean.clearLastAction();
        }


        int pauseCount = 0 ;

        Set<QuartzJobInfo> jobs = new TreeSet<QuartzJobInfo>();
        String[] groups = scheduler.getJobGroupNames();
        for (String group: groups) {
            String[] jobNames = scheduler.getJobNames(group);
            for (String jobName: jobNames) {
                Trigger[] triggers = scheduler.getTriggersOfJob(jobName, group);
                boolean paused = false;
                Date nextExecution = null;
                Date lastExecution = null;

                // We only need the first trigger to determine if paused
                if (triggers != null && triggers.length > 0) {
                    paused = scheduler.getTriggerState(triggers[0].getName(), triggers[0].getGroup()) == Trigger.STATE_PAUSED;
                    if(paused) ++pauseCount ; 
                    for (int t = 0; t < triggers.length; t++) {
                        nextExecution = triggers[t].getNextFireTime();
                        lastExecution = triggers[t].getPreviousFireTime();
                        if (nextExecution != null)
                            break;
                    }
                }

                jobs.add(new QuartzJobInfo(jobName, lastExecution, nextExecution, paused));
            }
        }
        quartzJobsCommandBean.setQuartzJobInfoList(jobs);

        if(scheduler.isStarted()){
            quartzJobsCommandBean.setJobsRunning( (pauseCount==jobs.size() ? false:true ) );
        }
        else{
            quartzJobsCommandBean.setJobsRunning( false );
        }

        return modelAndView;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
