package org.zfin.database;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class DbLock {

    private DatabaseLock syslock;
    private SysSession sysSession;

    public DatabaseLock getSyslock() {
        return syslock;
    }

    public void setSyslock(DatabaseLock syslock) {
        this.syslock = syslock;
    }

    public SysSession getSysSession() {
        return sysSession;
    }

    public void setSysSession(SysSession sysSession) {
        this.sysSession = sysSession;
    }
}
