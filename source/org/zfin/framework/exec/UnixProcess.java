package org.zfin.framework.exec;


import java.util.Date;

/**
 * Convenience class that lists certain unix processes.
 */
public class UnixProcess {

    private String command;
    private String user;
    private Date startDate;
    private String startDateString;
    private String durationString;
    private String terminal;
    private int pid;
    private int uid;
    private int cpuUsage;
    private int parentPid;

    public void addColumn(UnixProcessService.ProcessEfColumns column, String value) {
        switch (column) {
            case UID:
                uid = Integer.parseInt(value.trim());
                break;
            case PID:
                pid = Integer.parseInt(value.trim());
                break;
            case PPID:
                parentPid = Integer.parseInt(value.trim());
                break;
            case C:
                cpuUsage = Integer.parseInt(value.trim());
                break;
            case STIME:
                startDateString = value.trim();
                break;
            case TTY:
                terminal = value.trim();
                break;
            case TIME:
                durationString = value.trim();
                break;
            case CMD:
                command = value.trim();
                break;

        }
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getParentPid() {
        return parentPid;
    }

    public void setParentPid(int parentPid) {
        this.parentPid = parentPid;
    }
}
