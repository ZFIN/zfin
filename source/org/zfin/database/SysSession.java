package org.zfin.database;

import java.util.Set;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class SysSession {

    private String sid;
    private String userName;
    private Set<DatabaseLock> syslocks;

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Set<DatabaseLock> getSyslocks() {
        return syslocks;
    }

    public void setSyslocks(Set<DatabaseLock> syslocks) {
        this.syslocks = syslocks;
    }
}
