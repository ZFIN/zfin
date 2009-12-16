package org.zfin.framework.presentation;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.Random;

/**
 */
public class QuartzJobsBean {

    private String action ;
    private String job ;
    private String group ;
    private Set<QuartzJobInfo> quartzJobInfoList;
    private Set<QuartzJobInfo> manualJobsList ;
    private String message ;
    private boolean jobsRunning ;
    public static final int JOB_NOT_RUN = -1 ;
    private int requestID = JOB_NOT_RUN  ;
    private Random random = new Random() ;

    public static enum Action{
        RUN("run"),
        PAUSE("pause"),
        RESUME("resume"),
        PAUSE_ALL("pauseAll"),
        RESUME_ALL("resumeAll"),
        ;


        private final String value;

        private Action(String type) {
            this.value = type;
        }

        public String toString() {
            return this.value;
        }

        public static Action getType(String type) {
            for (Action t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No action of string [" + type + "] found.");
        }

        public boolean isPauseAction() {
            return value.equals(PAUSE.value) || value.equals(PAUSE_ALL.value) ;
        }


        public boolean isIndividualAction() {
            return value.equals(PAUSE.value) || value.equals(RUN.value) || value.equals(RESUME.value) ;
        }
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Set<QuartzJobInfo> getQuartzJobInfoList() {
        return quartzJobInfoList;
    }

    public void setQuartzJobInfoList(Set<QuartzJobInfo> quartzJobInfoList) {
        this.quartzJobInfoList = quartzJobInfoList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isJobsRunning() {
        return jobsRunning;
    }

    public void setJobsRunning(boolean jobsRunning) {
        this.jobsRunning = jobsRunning;
    }

    public Set<QuartzJobInfo> getManualJobsList() {
        return manualJobsList;
    }

    public void setManualJobsList(Set<QuartzJobInfo> manualJobsList) {
        this.manualJobsList = manualJobsList;
    }

    public void clearLastAction() {
        if(action!=null){
            this.message = action + " " + job ;
            this.group= null ;
            this.action= null ;
            this.job= null ;
        }
    }

    public int getRequestID() {
        return requestID;
    }

    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }

    public int getNewRequestID(){
        return random.nextInt() ;
    }
}
