package org.quartz.simpl;

import org.quartz.Calendar;
import org.quartz.*;
import org.quartz.core.SchedulingContext;
import org.quartz.spi.TriggerFiredBundle;

import java.util.*;

/**
 * Overrides the RAMJobStore from Quartz to facilitate a serial execution of stateful jobs withing
 * within the same group.
 */
public class GroupRAMJobStore extends RAMJobStore {

    @Override
    public TriggerFiredBundle triggerFired(SchedulingContext ctxt, Trigger trigger) {

        synchronized (triggerLock) {
            TriggerWrapper tw = (TriggerWrapper) triggersByFQN.get(TriggerWrapper
                    .getTriggerNameKey(trigger));
            // was the trigger deleted since being acquired?
            if (tw == null || tw.trigger == null) {
                return null;
            }
            // was the trigger completed, paused, blocked, etc. since being acquired?
            if (tw.state != TriggerWrapper.STATE_ACQUIRED) {
                return null;
            }

            Calendar cal = null;
            if (tw.trigger.getCalendarName() != null) {
                cal = retrieveCalendar(ctxt, tw.trigger.getCalendarName());
                if (cal == null)
                    return null;
            }
            Date prevFireTime = trigger.getPreviousFireTime();
            // in case trigger was replaced between acquiring and firring
            timeTriggers.remove(tw);
            // call triggered on our copy, and the scheduler's copy
            tw.trigger.triggered(cal);
            trigger.triggered(cal);
            //tw.state = TriggerWrapper.STATE_EXECUTING;
            tw.state = TriggerWrapper.STATE_WAITING;

            TriggerFiredBundle bndle = new TriggerFiredBundle(retrieveJob(ctxt,
                    trigger.getJobName(), trigger.getJobGroup()), trigger, cal,
                    false, new Date(), trigger.getPreviousFireTime(), prevFireTime,
                    trigger.getNextFireTime());

            JobDetail job = bndle.getJobDetail();

            if (job.isStateful()) {
                List<TriggerWrapper> groupTriggers = getTriggerWrappersForGroupJob(job.getGroup(), ctxt);
                for (TriggerWrapper ttw : groupTriggers) {
                    if (ttw.state == TriggerWrapper.STATE_WAITING) {
                        ttw.state = TriggerWrapper.STATE_BLOCKED;
                    }
                    if (ttw.state == TriggerWrapper.STATE_PAUSED) {
                        ttw.state = TriggerWrapper.STATE_PAUSED_BLOCKED;
                    }
                    timeTriggers.remove(ttw);
                }
                blockedJobs.add(JobWrapper.getJobNameKey(job));
            } else if (tw.trigger.getNextFireTime() != null) {
                synchronized (triggerLock) {
                    timeTriggers.add(tw);
                }
            }

            return bndle;
        }
    }

    protected List<TriggerWrapper> getTriggerWrappersForGroupJob(String groupName, SchedulingContext context) {
        String[] jobNames = getJobNames(context, groupName);
        List<TriggerWrapper> trigList = new ArrayList<TriggerWrapper>(jobNames.length);
        for (String jobName : jobNames) {
            String jobKey = JobWrapper.getJobNameKey(jobName, groupName);
            synchronized (triggerLock) {
                for (Object trigger : triggers) {
                    TriggerWrapper tw = (TriggerWrapper) trigger;
                    if (tw.jobKey.equals(jobKey)) {
                        trigList.add(tw);
                    }
                }
            }
        }
        return trigList;
    }

    /**
     * <p>
     * Inform the <code>JobStore</code> that the scheduler has completed the
     * firing of the given <code>Trigger</code> (and the execution its
     * associated <code>Job</code>), and that the <code>{@link org.quartz.JobDataMap}</code>
     * in the given <code>JobDetail</code> should be updated if the <code>Job</code>
     * is stateful.
     * </p>
     */
    @Override
    public void triggeredJobComplete(SchedulingContext ctxt, Trigger trigger,
                                     JobDetail jobDetail, int triggerInstCode) {

        synchronized (triggerLock) {

            String jobKey = JobWrapper.getJobNameKey(jobDetail.getName(), jobDetail
                    .getGroup());
            JobWrapper jw = (JobWrapper) jobsByFQN.get(jobKey);
            TriggerWrapper tw = (TriggerWrapper) triggersByFQN.get(TriggerWrapper
                    .getTriggerNameKey(trigger));

            // It's possible that the job is null if:
            //   1- it was deleted during execution
            //   2- RAMJobStore is being used only for volatile jobs / triggers
            //      from the JDBC job store
            if (jw != null) {
                JobDetail jd = jw.jobDetail;

                if (jd.isStateful()) {
                    JobDataMap newData = jobDetail.getJobDataMap();
                    if (newData != null) {
                        newData = (JobDataMap) newData.clone();
                        newData.clearDirtyFlag();
                    }
                    jd.setJobDataMap(newData);
                    blockedJobs.remove(JobWrapper.getJobNameKey(jd));
                    List<TriggerWrapper> trigs = getTriggerWrappersForJob(jd.getName(), jd.getGroup());
                    unblockTriggerWrappers(trigs);
                    List groupTriggers = getTriggerWrappersForGroupJob(jw.jobDetail.getGroup(), ctxt);
                    unblockTriggerWrappers(groupTriggers);
                    signaler.signalSchedulingChange(0L);
                }
            } else { // even if it was deleted, there may be cleanup to do
                blockedJobs.remove(JobWrapper.getJobNameKey(jobDetail));
            }

            // check for trigger deleted during execution...
            if (tw != null) {
                if (triggerInstCode == Trigger.INSTRUCTION_DELETE_TRIGGER) {

                    if (trigger.getNextFireTime() == null) {
                        // double check for possible reschedule within job
                        // execution, which would cancel the need to delete...
                        if (tw.getTrigger().getNextFireTime() == null) {
                            removeTrigger(ctxt, trigger.getName(), trigger.getGroup());
                        }
                    } else {
                        removeTrigger(ctxt, trigger.getName(), trigger.getGroup());
                        signaler.signalSchedulingChange(0L);
                    }
                } else if (triggerInstCode == Trigger.INSTRUCTION_SET_TRIGGER_COMPLETE) {
                    tw.state = TriggerWrapper.STATE_COMPLETE;
                    timeTriggers.remove(tw);
                    signaler.signalSchedulingChange(0L);
                } else if (triggerInstCode == Trigger.INSTRUCTION_SET_TRIGGER_ERROR) {
                    getLog().info("Trigger " + trigger.getFullName() + " set to ERROR state.");
                    tw.state = TriggerWrapper.STATE_ERROR;
                    signaler.signalSchedulingChange(0L);
                } else if (triggerInstCode == Trigger.INSTRUCTION_SET_ALL_JOB_TRIGGERS_ERROR) {
                    getLog().info("All triggers of Job "
                            + trigger.getFullJobName() + " set to ERROR state.");
                    setAllTriggersOfJobToState(
                            trigger.getJobName(),
                            trigger.getJobGroup(),
                            TriggerWrapper.STATE_ERROR);
                    signaler.signalSchedulingChange(0L);
                } else if (triggerInstCode == Trigger.INSTRUCTION_SET_ALL_JOB_TRIGGERS_COMPLETE) {
                    setAllTriggersOfJobToState(
                            trigger.getJobName(),
                            trigger.getJobGroup(),
                            TriggerWrapper.STATE_COMPLETE);
                    signaler.signalSchedulingChange(0L);
                }
            }
        }
    }

    private void unblockTriggerWrappers(List<TriggerWrapper> triggers) {
        for (TriggerWrapper ttw : triggers) {
            if (ttw.state == TriggerWrapper.STATE_BLOCKED) {
                ttw.state = TriggerWrapper.STATE_WAITING;
                timeTriggers.add(ttw);
            }
            if (ttw.state == TriggerWrapper.STATE_PAUSED_BLOCKED) {
                ttw.state = TriggerWrapper.STATE_PAUSED;
            }
        }
    }

    /**
     * <p>
     * Store the given <code>{@link org.quartz.Trigger}</code>.
     * </p>
     *
     * @param newTrigger      The <code>Trigger</code> to be stored.
     * @param replaceExisting If <code>true</code>, any <code>Trigger</code> existing in
     *                        the <code>JobStore</code> with the same name & group should
     *                        be over-written.
     * @throws org.quartz.ObjectAlreadyExistsException
     *          if a <code>Trigger</code> with the same name/group already
     *          exists, and replaceExisting is set to false.
     * @see #pauseTriggerGroup(SchedulingContext, String)
     */
    @Override
    public void storeTrigger(SchedulingContext ctxt, Trigger newTrigger,
                             boolean replaceExisting) throws JobPersistenceException {
        TriggerWrapper tw = new TriggerWrapper((Trigger) newTrigger.clone());

        if (triggersByFQN.get(tw.key) != null) {
            if (!replaceExisting) {
                throw new ObjectAlreadyExistsException(newTrigger);
            }

            removeTrigger(ctxt, newTrigger.getName(), newTrigger.getGroup(), false);
        }

        JobDetail jobDetail = retrieveJob(ctxt, newTrigger.getJobName(), newTrigger.getJobGroup());
        if (jobDetail == null) {
            throw new JobPersistenceException("The job ("
                    + newTrigger.getFullJobName()
                    + ") referenced by the trigger does not exist.");
        }

        synchronized (triggerLock) {
            // add to triggers array
            triggers.add(tw);
            // add to triggers by group
            HashMap grpMap = (HashMap) triggersByGroup.get(newTrigger
                    .getGroup());
            if (grpMap == null) {
                grpMap = new HashMap(100);
                triggersByGroup.put(newTrigger.getGroup(), grpMap);
            }
            grpMap.put(newTrigger.getName(), tw);
            // add to triggers by FQN map
            triggersByFQN.put(tw.key, tw);

            if (pausedTriggerGroups.contains(newTrigger.getGroup())
                    || pausedJobGroups.contains(newTrigger.getJobGroup())) {
                tw.state = TriggerWrapper.STATE_PAUSED;
                if (blockedJobs.contains(tw.jobKey)) {
                    tw.state = TriggerWrapper.STATE_PAUSED_BLOCKED;
                }
            } else if (blockedJobs.contains(tw.jobKey)) {
                tw.state = TriggerWrapper.STATE_BLOCKED;
            } else if (jobDetail.isStateful()) {
                List<TriggerWrapper> groupTriggers = getTriggerWrappersForGroupJob(jobDetail.getGroup(), ctxt);
                boolean jobBlocked = false;
                for (TriggerWrapper wrapper : groupTriggers) {
                    JobDetail detail = retrieveJob(ctxt, wrapper.getTrigger().getJobName(), wrapper.getTrigger().getJobGroup());
                    if (detail != null && detail.getFullName().equals(jobDetail.getFullName()))
                        if (wrapper.state == TriggerWrapper.STATE_BLOCKED) {
                            tw.state = TriggerWrapper.STATE_BLOCKED;
                            jobBlocked = true;
                        }
                }
                if (!jobBlocked)
                    timeTriggers.add(tw);
            } else {
                timeTriggers.add(tw);
            }
        }
    }

    private boolean removeTrigger(SchedulingContext ctxt, String triggerName,
                                  String groupName, boolean removeOrphanedJob) {
        String key = TriggerWrapper.getTriggerNameKey(triggerName, groupName);

        boolean found = false;

        synchronized (triggerLock) {
            // remove from triggers by FQN map
            found = (triggersByFQN.remove(key) == null) ? false : true;
            if (found) {
                TriggerWrapper tw = null;
                // remove from triggers by group
                HashMap grpMap = (HashMap) triggersByGroup.get(groupName);
                if (grpMap != null) {
                    grpMap.remove(triggerName);
                    if (grpMap.size() == 0) {
                        triggersByGroup.remove(groupName);
                    }
                }
                // remove from triggers array
                Iterator tgs = triggers.iterator();
                while (tgs.hasNext()) {
                    tw = (TriggerWrapper) tgs.next();
                    if (key.equals(tw.key)) {
                        tgs.remove();
                        break;
                    }
                }
                timeTriggers.remove(tw);

                if (removeOrphanedJob) {
                    JobWrapper jw = (JobWrapper) jobsByFQN.get(JobWrapper
                            .getJobNameKey(tw.trigger.getJobName(), tw.trigger
                            .getJobGroup()));
                    Trigger[] trigs = getTriggersForJob(ctxt, tw.trigger
                            .getJobName(), tw.trigger.getJobGroup());
                    if ((trigs == null || trigs.length == 0) && !jw.jobDetail.isDurable()) {
                        removeJob(ctxt, tw.trigger.getJobName(), tw.trigger
                                .getJobGroup());
                    }
                }
            }
        }

        return found;
    }
}
