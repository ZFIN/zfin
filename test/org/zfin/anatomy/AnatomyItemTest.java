package org.zfin.anatomy;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import org.junit.Test;

import java.io.Serializable;

/**
 * // ToDo: Need to add more in-depth tests.
 */
public class AnatomyItemTest implements Serializable {

    @Test
    public void equality() {
        AnatomyItem one = new AnatomyItem();
        one.setName("root");

        AnatomyItem oneOne = new AnatomyItem();
        oneOne.setName("root");

        AnatomyItem two = new AnatomyItem();
        two.setName("child");

        assertEquals("one = one", one, one);
        assertEquals("one = oneOne", one, oneOne);
        assertEquals("two = two", two, two);
        assertNotSame("one != two", one, two);

    }

}