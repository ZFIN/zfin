package org.zfin.database;

import java.util.*;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class SysSession {

    private int sid;
    private String userName;
    private long uid;
    private long pid;
    private String hostname;
    private String tty;
    private long connected;
    private int transaction;
    private int lock;
    private int latch;
    private int buffer;
    private int logbuffer;
    private int checkpoint;
    private int monitor;
    private int critical;
    private int state;
    private int poolAddress;

    private Date startDate;
    private Set<DatabaseLock> syslocks;
    private SysOpenDb sysOpenDb;
    private Set<SysOpenDb> sysOpenDbList;

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
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

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getPid() {
        return pid;
    }

    public void setPid(long pid) {
        this.pid = pid;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getTty() {
        return tty;
    }

    public void setTty(String tty) {
        this.tty = tty;
    }

    public long getConnected() {
        return connected;
    }

    public void setConnected(long connected) {
        this.connected = connected;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public SysOpenDb getSysOpenDb() {
        return sysOpenDb;
    }

    public void setSysOpenDb(SysOpenDb sysOpenDb) {
        this.sysOpenDb = sysOpenDb;
    }

    public int getPoolAddress() {
        return poolAddress;
    }

    public void setPoolAddress(int poolAddress) {
        this.poolAddress = poolAddress;
    }

    public int getTransaction() {
        return transaction;
    }

    private String flagMessage = "primary thread for the session is waiting for a ";

    public List<String> getFlagStatus() {
        List<String> flags = new ArrayList<String>(2);
        if (transaction == 1)
            flags.add(flagMessage + "transaction");
        if (latch == 1)
            flags.add(flagMessage + "latch");
        if (lock == 1)
            flags.add(flagMessage + "lock");
        if (buffer == 1)
            flags.add(flagMessage + "buffer");
        if (checkpoint == 1)
            flags.add(flagMessage + "checkpoint");
        if (logbuffer == 1)
            flags.add(flagMessage + "log buffer");
        if (monitor == 1)
            flags.add("Session is a special monitoring process");
        if (critical == 1)
            flags.add("Primary thread for this session is in a critical section");

        return flags;
    }


    public void setTransaction(int transaction) {
        this.transaction = transaction;
    }

    public int getLock() {
        return lock;
    }

    public void setLock(int lock) {
        this.lock = lock;
    }

    public int getLatch() {
        return latch;
    }

    public void setLatch(int latch) {
        this.latch = latch;
    }

    public int getBuffer() {
        return buffer;
    }

    public void setBuffer(int buffer) {
        this.buffer = buffer;
    }

    public int getLogbuffer() {
        return logbuffer;
    }

    public void setLogbuffer(int logbuffer) {
        this.logbuffer = logbuffer;
    }

    public int getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(int checkpoint) {
        this.checkpoint = checkpoint;
    }

    public int getMonitor() {
        return monitor;
    }

    public void setMonitor(int monitor) {
        this.monitor = monitor;
    }

    public int getCritical() {
        return critical;
    }

    public void setCritical(int critical) {
        this.critical = critical;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public List<State> getListOfStates() {
        return State.getListOfStates(state);
    }

    public static Date toPstFromGMT(Date date) {
        TimeZone pst = TimeZone.getTimeZone("PST");
        return new Date(date.getTime() + pst.getRawOffset());
    }

    public Set<SysOpenDb> getSysOpenDbList() {
        return sysOpenDbList;
    }

    public void setSysOpenDbList(Set<SysOpenDb> sysOpenDbList) {
        this.sysOpenDbList = sysOpenDbList;
    }

    public enum State implements Comparable<State> {
        USER_STRUCTURE(1, "User structure in use"),
        WAITING_FOR_LATCH(2, "Waiting for a latch"),
        WAITING_FOR_LOCK(4, "Waiting for a lock"),
        WAITING_FOR_BUFFER(8, "Waiting for a buffer"),
        WAITING_CHECKPOINT(16, "Waiting for a checkpoint"),
        READ_CALL(32, "In a read call"),
        WRITING_LOGICAL_LOG(64, "Writing logical-log file to backup tape"),
        ON_MONITOR(128, "ON-Monitor"),
        CRITICAL_SECTION(256, "In a critical section"),
        SPECIAL_DAEMON(512, "Special Daemon"),
        Archiving(1024, "Archiving"),
        CLEANUP_DEAD_PROCESS(2048, "Clean up dead process"),
        WRITE_LOG_BUFFER(4096, "Waiting for write of log buffer"),
        THREAD_BUFFER_FLUSHING(8192, "Special buffer-flushing thread"),
        REMOTE_DATABASE(16384, "Remote Database Server"),
        DEADLOCK(32768, "Deadlock timeout used to set RS_timeout"),
        LOCK_TIMEOUT(65536, "Regular lock timeout"),
        WAITING_FOR_TRANSACTION(262144, "Waiting for a transaction"),
        SESSION(524288, "Primary thread for a session"),
        INDEXER_THREAD(1048576, "Thread for building indexes"),
        CLEANER_THREAD(2097152, "B-tree cleaner thread"),;

        private String message;
        private int flag;

        private State(int flag, String message) {
            this.message = message;
            this.flag = flag;
        }

        public static List<State> getListOfStates(int stateFlag) {
            List<State> states = new ArrayList<State>(5);
            int runningFlag = stateFlag;
            for (State state : getAllStatesDescendingList()) {
                if (state.flag > runningFlag)
                    continue;
                states.add(state);
                runningFlag -= state.flag;
            }
            return states;
        }

        public static List<State> getAllStatesList() {
            List<State> states = new ArrayList<State>(5);
            Collections.addAll(states, values());
            Collections.sort(states);
            return states;
        }

        public static List<State> getAllStatesDescendingList() {
            List<State> states = new ArrayList<State>(5);
            Collections.addAll(states, values());
            Collections.sort(states);
            Collections.reverse(states);
            return states;
        }

        public String getMessage() {
            return message;
        }
    }

}
