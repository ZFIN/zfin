package org.zfin.framework.presentation;

import org.apache.commons.lang.ObjectUtils;

import java.util.Date;

/**
 */
public class QuartzJobInfo implements Comparable<QuartzJobInfo> {

    private String name;
    private String group;
    private Date lastExecution;
    private Date nextExecution;
    private boolean paused;
    private boolean running;

    public QuartzJobInfo(String name, Date lastExecution, Date nextExecution, boolean paused, String group) {
        this.name = name;
        this.group = group;
        this.lastExecution = lastExecution;
        this.nextExecution = nextExecution;
        this.paused = paused;
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
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

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int compareTo(QuartzJobInfo o) {
        if (o == null)
            return 1;

        if (!ObjectUtils.equals(group, o.getGroup()))
            return group.compareTo(o.getGroup());
        if (nextExecution == null && o.getNextExecution() != null)
            return +1;
        if (nextExecution != null && o.getNextExecution() == null)
            return -1;
        if (nextExecution != null && o.getNextExecution() != null) {
            if (nextExecution.compareTo(o.getNextExecution()) != 0)
                return nextExecution.compareTo(o.getNextExecution());
        }
        return name.compareTo(o.getName());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("QuartzJobInfo");
        sb.append("{name='").append(name).append('\'');
        sb.append(", group='").append(group).append('\'');
        sb.append(", lastExecution=").append(lastExecution);
        sb.append(", nextExecution=").append(nextExecution);
        sb.append(", paused=").append(paused);
        sb.append('}');
        return sb.toString();
    }

}
