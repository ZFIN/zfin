package org.zfin.database;

import org.apache.log4j.Logger;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.Table;
import org.zfin.database.repository.SysmasterRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class: Retrieve
 */
public class DbSystemUtil {

    private static final CellStyle rightAlign = new CellStyle(CellStyle.HorizontalAlign.right);
    private static final Logger LOG = Logger.getLogger("database locking");

    /**
     * Log info about locks in the database.
     * This method is catching any exception to not cause an
     * exception in the main thread execution.
     */
    public static void logLockInfo() {
        if (LOG.isDebugEnabled())
            LOG.debug(getLockInfo());
    }

    /**
     * Collect info about locks in the database.
     * This method is catching any exception to not cause an
     * exception in the main thread execution.
     *
     * @return formatted lock info
     */
    public static String getLockInfo() {
        try {
            List<DatabaseLock> locks = SysmasterRepository.getLocks();
            if (locks == null)
                return "No Locks";

            Table output = new Table(4);
            output.addCell("Table Name");
            output.addCell("Lock Type");
            output.addCell("# of Locks");
            output.addCell("Lock Scope");
            List<TableLock> summaryLocks = getLockSummary(locks);
            Collections.sort(summaryLocks);
            for (TableLock lock : summaryLocks) {
                output.addCell(lock.getTableName().trim());
                output.addCell(lock.getType());
                output.addCell("" + lock.getNumOfLocks(), rightAlign);
                output.addCell(lock.getRowLength());
            }
            return System.getProperty("line.separator") + output.render();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * We are interested in how many locks there are per table and do not want to
     * see each individual one which could be a large number.
     *
     * @param locks list of row locks
     * @return collection of TableLock objects
     */
    private static List<TableLock> getLockSummary(List<DatabaseLock> locks) {
        if (locks == null)
            return null;
        List<TableLock> tableLocks = new ArrayList<TableLock>(locks.size());
        for (DatabaseLock rowLock : locks) {
            TableLock tableLock = getTableLock(rowLock);
            if (tableLocks.contains(tableLock)) {
                TableLock lock = getTableLockFromList(tableLock, tableLocks);
                lock.incrementLockCounter();
            } else {
                tableLocks.add(tableLock);
            }
        }
        return tableLocks;
    }

    private static TableLock getTableLockFromList(TableLock tableLock, List<TableLock> tableLocks) {
        if (tableLocks == null)
            return null;
        if (tableLock == null)
            return null;

        for (TableLock tableLockFromList : tableLocks) {
            if (tableLock.equals(tableLockFromList))
                return tableLockFromList;
        }
        return null;
    }

    private static TableLock getTableLock(DatabaseLock rowLock) {
        if (rowLock == null)
            return null;

        TableLock tableLock = new TableLock();
        tableLock.setDbsName(rowLock.getDbsName());
        tableLock.setSession(rowLock.getSession());
        tableLock.setTableName(rowLock.getTableName());
        tableLock.setType(rowLock.getType());
        tableLock.setRowId(rowLock.getRowId());
        tableLock.setNumOfLocks(1);
        return tableLock;
    }
}
