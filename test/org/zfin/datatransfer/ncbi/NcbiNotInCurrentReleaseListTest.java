package org.zfin.datatransfer.ncbi;

import org.junit.Test;
import org.zfin.datatransfer.ncbi.NcbiNotInCurrentReleaseList.Result;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NcbiNotInCurrentReleaseListTest {

    @Test
    public void emptyFetchIsNeverStored() {
        assertEquals(Result.KEPT_EMPTY_FETCH, NcbiNotInCurrentReleaseList.decide(2800, 0));
        assertEquals(Result.KEPT_EMPTY_FETCH, NcbiNotInCurrentReleaseList.decide(0, 0));
    }

    @Test
    public void firstFillIsStored() {
        assertEquals(Result.REPLACED, NcbiNotInCurrentReleaseList.decide(0, 2800));
    }

    @Test
    public void growthAndSmallShrinkAreStored() {
        assertEquals(Result.REPLACED, NcbiNotInCurrentReleaseList.decide(2800, 3000));
        assertEquals(Result.REPLACED, NcbiNotInCurrentReleaseList.decide(2800, 2500));
    }

    @Test
    public void exactlyHalfIsStoredButLessThanHalfIsNot() {
        assertEquals(Result.REPLACED, NcbiNotInCurrentReleaseList.decide(2800, 1400));
        assertEquals(Result.KEPT_SHRUNK, NcbiNotInCurrentReleaseList.decide(2800, 1399));
    }

    @Test
    public void normalizeTrimsDropsBlanksAndDuplicatesAndSorts() {
        List<String> cleaned = NcbiNotInCurrentReleaseList.normalize(
                Arrays.asList("30", " 12 ", null, "", "  ", "12", "100"));
        assertEquals(List.of("100", "12", "30"), cleaned);
    }

    @Test
    public void normalizeAcceptsNull() {
        assertTrue(NcbiNotInCurrentReleaseList.normalize(null).isEmpty());
    }
}
