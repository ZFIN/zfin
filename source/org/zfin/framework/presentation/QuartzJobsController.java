package org.zfin.framework.presentation;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
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
 * Liberally borrowed from the Confluence Plugin Job Manager, JobManagerAction class.
 * This controller manages and watches quartz jobs and their queue.
 */
public class QuartzJobsController extends AbstractCommandController {

    private Scheduler scheduler;

    public static final String MANUAL_TRIGGER_GROUP = "MANUAL_TRIGGER";
    private Integer alreadyRunRequestID = -10;

    protected ModelAndView handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, BindException e) throws Exception {
        ModelAndView modelAndView = new ModelAndView("quartz-jobs.page");

        QuartzJobsBean quartzJobsBean = (QuartzJobsBean) o;
        modelAndView.addObject(LookupStrings.FORM_BEAN, quartzJobsBean);


        if (alreadyRunRequestID != quartzJobsBean.getRequestID()) {
            logger.debug("RUNNING: " + quartzJobsBean.getRequestID() + " vs " + alreadyRunRequestID);
            handleAction(quartzJobsBean);
            alreadyRunRequestID = quartzJobsBean.getRequestID();
        } else {
            logger.debug("ALREADY RUN: " + alreadyRunRequestID);
        }

        retrieveJobDetails(quartzJobsBean);
        retrieveManualJobs(quartzJobsBean);

        return modelAndView;
    }

    private void handleAction(QuartzJobsBean quartzJobsBean) throws SchedulerException {

        if (quartzJobsBean.getAction() != null) {
            QuartzJobsBean.Action action = QuartzJobsBean.Action.getType(quartzJobsBean.getAction());

            // if we have a valid action that is not pauseAll
            if (false == action.isPauseAction()) {
                if (false == scheduler.isStarted()) {
                    scheduler.start();
                }
            }

            if (action.isIndividualAction() && quartzJobsBean.getJob() != null) {
                String job = quartzJobsBean.getJob();
                String group = quartzJobsBean.getGroup();
                if (group == null) {
                    group = "DEFAULT";
                }
                logger.info("Action :" + action + " job: " + job);

                if (action == QuartzJobsBean.Action.RUN) {
                    scheduler.triggerJob(job, group);
                } else if (action == QuartzJobsBean.Action.PAUSE) {
                    scheduler.pauseJob(job, group);
                } else if (action == QuartzJobsBean.Action.RESUME) {
                    scheduler.resumeJob(job, group);
                }
            } else {
                logger.info("ALL action :" + action);
                if (action == QuartzJobsBean.Action.PAUSE_ALL) {
                    scheduler.pauseAll();
                } else if (action == QuartzJobsBean.Action.RESUME_ALL) {
                    scheduler.resumeAll();
                }
            }
        }
        quartzJobsBean.clearLastAction();
    }

    private void retrieveManualJobs(QuartzJobsBean quartzJobsBean) throws SchedulerException {
        Set<QuartzJobInfo> jobs = new TreeSet<QuartzJobInfo>();
        String[] manualTriggerNames = scheduler.getTriggerNames(MANUAL_TRIGGER_GROUP);
        for (String triggerName : manualTriggerNames) {
            Trigger trigger = scheduler.getTrigger(triggerName, MANUAL_TRIGGER_GROUP);
            jobs.add(new QuartzJobInfo(trigger.getJobName(), trigger.getPreviousFireTime(),
                    trigger.getNextFireTime(), scheduler.getTriggerState(triggerName, MANUAL_TRIGGER_GROUP) == Trigger.STATE_PAUSED));
        }
        quartzJobsBean.setManualJobsList(jobs);
    }


    private QuartzJobsBean retrieveJobDetails(QuartzJobsBean quartzJobsBean) throws SchedulerException {

        int pauseCount = 0;
        Set<QuartzJobInfo> jobs = new TreeSet<QuartzJobInfo>();
        String[] groups = scheduler.getJobGroupNames();
        for (String group : groups) {
            String[] jobNames = scheduler.getJobNames(group);
            for (String jobName : jobNames) {
                Trigger[] triggers = scheduler.getTriggersOfJob(jobName, group);
                boolean paused = false;
                Date nextExecution = null;
                Date lastExecution = null;

                // We only need the first trigger to determine if paused
                if (triggers != null && triggers.length > 0) {
                    paused = scheduler.getTriggerState(triggers[0].getName(), triggers[0].getGroup()) == Trigger.STATE_PAUSED;
                    if (paused) {
                        ++pauseCount;
                    }
                    for (Trigger trigger : triggers) {
                        nextExecution = trigger.getNextFireTime();
                        lastExecution = trigger.getPreviousFireTime();
                        if (nextExecution != null) {
                            break;
                        }
                    }
                }

                jobs.add(new QuartzJobInfo(jobName, lastExecution, nextExecution, paused));
            }
        }

        quartzJobsBean.setQuartzJobInfoList(jobs);

        if (scheduler.isStarted()) {
            quartzJobsBean.setJobsRunning(pauseCount != jobs.size());
        } else {
            quartzJobsBean.setJobsRunning(false);
        }
        return quartzJobsBean;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
