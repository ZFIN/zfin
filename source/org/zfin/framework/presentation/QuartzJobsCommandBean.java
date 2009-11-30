package org.zfin.framework.presentation;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Set;

/**
 */
public class QuartzJobsCommandBean {

    private String action ;
    private String job ;
    private String group ;
    private Set<QuartzJobInfo> quartzJobInfoList;
    private String message ;
    private boolean jobsRunning ;

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

    public void clearLastAction() {
        if(StringUtils.isNotEmpty(action)){
            this.message = action + " " + job ;
            this.group= null ;
            this.action= null ;
            this.job= null ;
        }
    }
}
