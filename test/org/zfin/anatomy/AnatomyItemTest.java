package org.zfin.anatomy;

import org.junit.Test;

import java.io.Serializable;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

/**
 * // ToDo: Need to add more in-depth tests.
 */
public class AnatomyItemTest implements Serializable {

    @Test
    public void equality() {
        AnatomyItem one = new AnatomyItem();
        one.setTermName("root");

        AnatomyItem oneOne = new AnatomyItem();
        oneOne.setTermName("root");

        AnatomyItem two = new AnatomyItem();
        two.setTermName("child");

        assertEquals("one = one", one, one);
        assertEquals("one = oneOne", one, oneOne);
        assertEquals("two = two", two, two);
        assertNotSame("one != two", one, two);

    }

}