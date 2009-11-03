package org.zfin.framework.presentation;

import org.quartz.Job;
import org.quartz.JobDetail;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

/**
 */
public class QuartzJobInfo implements Comparable<QuartzJobInfo>{

    private String name ;
    private Date lastExecution ;
    private Date nextExecution ;
    private boolean paused ;

    public QuartzJobInfo(String name, Date lastExecution, Date nextExecution,boolean paused){
        this.name = name ;
        this.lastExecution = lastExecution ;
        this.nextExecution = nextExecution ;
        this.paused = paused ;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastExecution() {
        return lastExecution;
    }

    public void setLastExecution(Date lastExecution) {
        this.lastExecution = lastExecution;
    }

    public Date getNextExecution() {
        return nextExecution;
    }

    public void setNextExecution(Date nextExecution) {
        this.nextExecution = nextExecution;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public int compareTo(QuartzJobInfo o) {
        return name.compareTo(o.getName()) ;
    }
}
