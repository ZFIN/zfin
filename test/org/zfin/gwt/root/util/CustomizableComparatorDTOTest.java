package org.zfin.gwt.root.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class CustomizableComparatorDTOTest {

    @Test
    public void orderOfAnyStringValues() {
        CustomizableComparatorDTO comparator = new CustomizableComparatorDTO();
        assertEquals("alpha vor Zeta", -25, comparator.compare("alpha", "Zeta"));
        assertEquals("alpha vor Zeta", -25, comparator.compare("Alpha", "Zeta"));
        assertEquals("alpha vor Zeta", 24, comparator.compare("yeta", "alpha"));
    }

    @Test
    public void orderOfStartAndEnd() {
        CustomizableComparatorDTO comparator = new CustomizableComparatorDTO();
        comparator.addToBottom("Start Stage");
        comparator.addToBottom("End Stage");
        assertEquals("alpha vor Zeta", -1, comparator.compare("alpha", "End stage"));
        assertEquals("alpha vor Zeta", -1, comparator.compare("alpha", "Start stage"));
        assertEquals("alpha vor Zeta", +1, comparator.compare("End Stage", "Start stage"));
        assertEquals("alpha vor Zeta", -25, comparator.compare("Alpha", "Zeta"));

    }

    @Test
    public void orderOfStartAndEndWithCustom() {
        RelationshipComparatorDTO comparator = new RelationshipComparatorDTO();
        assertEquals("alpha vor Zeta", -1, comparator.compare("alpha", "End stage"));
        assertEquals("alpha vor Zeta", -1, comparator.compare("alpha", "Start stage"));
        assertEquals("alpha vor Zeta", +1, comparator.compare("End Stage", "Start stage"));
        assertEquals("alpha vor Zeta", -25, comparator.compare("Alpha", "Zeta"));

    }
}