package org.zfin.database;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SysSessionTest {

    @Test
    public void sortStatesByFlag() {
        List<SysSession.State> states = SysSession.State.getAllStatesList();
        assertNotNull(states);
        assertEquals(21, states.size());

        assertEquals(SysSession.State.USER_STRUCTURE, states.get(0));
        assertEquals(SysSession.State.CLEANER_THREAD, states.get(20));
    }

    @Test
    public void descendingSortStatesByFlag() {
        List<SysSession.State> states = SysSession.State.getAllStatesDescendingList();
        assertNotNull(states);
        assertEquals(21, states.size());

        assertEquals(SysSession.State.CLEANER_THREAD, states.get(0));
        assertEquals(SysSession.State.USER_STRUCTURE, states.get(20));
    }

    @Test
    public void getStatesByFlag() {
        List<SysSession.State> states = SysSession.State.getListOfStates(1);
        assertEquals(1, states.size());
        assertEquals(SysSession.State.USER_STRUCTURE, states.get(0));

        states = SysSession.State.getListOfStates(5);
        assertEquals(2, states.size());
        assertEquals(SysSession.State.WAITING_FOR_LOCK, states.get(0));
        assertEquals(SysSession.State.USER_STRUCTURE, states.get(1));

        states = SysSession.State.getListOfStates(22678);
        assertEquals(7, states.size());
        assertEquals(SysSession.State.REMOTE_DATABASE, states.get(0));
        assertEquals(SysSession.State.WRITE_LOG_BUFFER, states.get(1));
        assertEquals(SysSession.State.CLEANUP_DEAD_PROCESS, states.get(2));
        assertEquals(SysSession.State.ON_MONITOR, states.get(3));
        assertEquals(SysSession.State.WAITING_CHECKPOINT, states.get(4));
        assertEquals(SysSession.State.WAITING_FOR_LOCK, states.get(5));
        assertEquals(SysSession.State.WAITING_FOR_LATCH, states.get(6));
    }
}
