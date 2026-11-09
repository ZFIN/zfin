package org.zfin.datatransfer.ncbi;

import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.HibernateUtil;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Runs inside the transaction {@link AbstractDatabaseTest} rolls back, so the table is left as it was.
 */
public class NcbiNotInCurrentReleaseListDatabaseTest extends AbstractDatabaseTest {

    private final NcbiNotInCurrentReleaseList list = new NcbiNotInCurrentReleaseList();

    @Before
    public void emptyTheTable() {
        HibernateUtil.currentSession()
                .createNativeQuery("delete from " + NcbiNotInCurrentReleaseList.TABLE)
                .executeUpdate();
    }

    @Test
    public void storesAFirstFetch() {
        NcbiNotInCurrentReleaseList.Outcome outcome = list.refresh(List.of("4", "3", "2", "1", "2"));
        assertEquals(NcbiNotInCurrentReleaseList.Result.REPLACED, outcome.result());
        assertEquals(List.of("1", "2", "3", "4"), list.storedIds());
        assertEquals(List.of("1", "2", "3", "4"), outcome.effectiveIds());
    }

    @Test
    public void keepsTheStoredListWhenTheFetchIsEmpty() {
        list.refresh(List.of("1", "2", "3", "4"));
        NcbiNotInCurrentReleaseList.Outcome outcome = list.refresh(List.of());
        assertEquals(NcbiNotInCurrentReleaseList.Result.KEPT_EMPTY_FETCH, outcome.result());
        assertEquals(List.of("1", "2", "3", "4"), list.storedIds());
        assertEquals(List.of("1", "2", "3", "4"), outcome.effectiveIds());
    }

    @Test
    public void keepsTheStoredListWhenTheFetchShrinksByMoreThanHalf() {
        list.refresh(List.of("1", "2", "3", "4"));
        NcbiNotInCurrentReleaseList.Outcome outcome = list.refresh(List.of("1"));
        assertEquals(NcbiNotInCurrentReleaseList.Result.KEPT_SHRUNK, outcome.result());
        assertEquals(List.of("1", "2", "3", "4"), list.storedIds());
        assertEquals(List.of("1", "2", "3", "4"), outcome.effectiveIds());
    }

    @Test
    public void replacesTheStoredListWhenTheFetchIsAtLeastHalfAsLarge() {
        list.refresh(List.of("1", "2", "3", "4"));
        NcbiNotInCurrentReleaseList.Outcome outcome = list.refresh(List.of("1", "2", "9"));
        assertEquals(NcbiNotInCurrentReleaseList.Result.REPLACED, outcome.result());
        assertEquals(List.of("1", "2", "9"), list.storedIds());
        assertEquals(4, outcome.previousCount());
    }
}
