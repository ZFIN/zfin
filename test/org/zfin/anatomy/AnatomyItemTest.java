package org.zfin.anatomy;

import org.junit.Test;
import org.zfin.ontology.GenericTerm;

import java.io.Serializable;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

/**
 * // ToDo: Need to add more in-depth tests.
 */
public class AnatomyItemTest implements Serializable {

    @Test
    public void equality() {
        GenericTerm one = new GenericTerm();
        one.setTermName("root");

        GenericTerm oneOne = new GenericTerm();
        oneOne.setTermName("root");

        GenericTerm two = new GenericTerm();
        two.setTermName("child");

        assertEquals("one = one", one, one);
        assertEquals("one = oneOne", one, oneOne);
        assertEquals("two = two", two, two);
        assertNotSame("one != two", one, two);

    }

}