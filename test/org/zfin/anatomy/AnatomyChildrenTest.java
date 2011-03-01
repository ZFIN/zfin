package org.zfin.anatomy;

import org.junit.Test;

import java.io.Serializable;

import static junit.framework.Assert.assertEquals;

/**
 */
public class AnatomyChildrenTest implements Serializable {

    @Test
    public void equality() {
        AnatomyItem root = new AnatomyItem();
        root.setTermName("root");

        AnatomyItem child = new AnatomyItem();
        child.setTermName("child");

        AnatomyChildren one = new AnatomyChildren();
        one.setRoot(root);
        one.setChild(child);
        AnatomyChildren two = new AnatomyChildren();
        two.setRoot(root);
        two.setChild(child);

        assertEquals("one = two", one, two);

    }

}