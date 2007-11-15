package org.zfin.util;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that allows to create formatted string lists from string items.
 * Default delimiter between two items is a comma and a space <, >.
 * The last delimiter is removed when the composed string is retrieved.
 */
public class ListFormatterTest{


    /**
     * Test default configuration: no item
     */
    @Test
    public void noItem(){
        ListFormatter formatter = new ListFormatter();

        String formattedString = formatter.getFormattedString();
        assertEquals("no comma no space", "", formattedString);
    }

    /**
     * Test default configuration: one item
     */
    @Test
    public void oneItem(){
        ListFormatter formatter = new ListFormatter();
        formatter.addItem("First Item");

        String formattedString = formatter.getFormattedString();
        assertEquals("no comma no space", "First Item", formattedString);
    }

    /**
     * Test default configuration: three items
     */
    @Test
    public void threeItems(){
        ListFormatter formatter = new ListFormatter();
        formatter.addItem("First Item");
        formatter.addItem("Second Item");
        formatter.addItem("Third Item");

        String formattedString = formatter.getFormattedString();
        assertEquals("comma delimited with spaces", "First Item, Second Item, Third Item", formattedString);
    }

    /**
     * Test no-space configuration: no item
     */
    @Test
    public void noItemNoSpace(){
        ListFormatter formatter = new ListFormatter(",");

        String formattedString = formatter.getFormattedString();
        assertEquals("no comma no space", "", formattedString);
    }

    /**
     * Test no-space  configuration: one item
     */
    @Test
    public void oneItemNoSpace(){
        ListFormatter formatter = new ListFormatter(",");
        formatter.addItem("First Item");

        String formattedString = formatter.getFormattedString();
        assertEquals("no comma no space", "First Item", formattedString);
    }

    /**
     * Test no-space  configuration: three items
     */
    @Test
    public void threeItemsNoSpace(){
        ListFormatter formatter = new ListFormatter(",");
        formatter.addItem("First Item");
        formatter.addItem("Second Item");
        formatter.addItem("Third Item");

        String formattedString = formatter.getFormattedString();
        assertEquals("comma delimited without spaces", "First Item,Second Item,Third Item", formattedString);
    }

    /**
     * Test no-space, escaped configuration: no item
     */
    @Test
    public void noItemNoSpaceEscape(){
        ListFormatter formatter = new ListFormatter(",", '\'');

        String formattedString = formatter.getFormattedString();
        assertEquals("no comma no space", "", formattedString);
    }

    /**
     * Test no-space  configuration: one item
     */
    @Test
    public void nneItemNoSpaceEscape(){
        ListFormatter formatter = new ListFormatter(",", '\'');
        formatter.addItem("First Item");

        String formattedString = formatter.getFormattedString();
        assertEquals("no comma no space", "'First Item'", formattedString);
    }

    /**
     * Test no-space  configuration: three items
     */
    @Test
    public void nhreeItemsNoSpaceEscape(){
        ListFormatter formatter = new ListFormatter(",", '\'');
        formatter.addItem("First Item");
        formatter.addItem("Second Item");
        formatter.addItem("Third Item");

        String formattedString = formatter.getFormattedString();
        assertEquals("comma delimited without spaces", "'First Item','Second Item','Third Item'", formattedString);
    }

    /**
     * Test default configuration, list of three items
     */
    @Test
    public void stringListThreeItems(){
        List<String> list = new ArrayList<String>();
        list.add("first item");
        list.add("second item");
        list.add("third item");

        ListFormatter formatter = new ListFormatter();
        formatter.addStringList(list);
        String formattedString = formatter.getFormattedString();
        assertEquals("comma delimited with spaces", "first item, second item, third item", formattedString);
    }

    /**
     * Test no-space configuration, list of three items, escaped.
     */
    @Test
    public void stringListThreeItemsNoSpaces(){
        List<String> list = new ArrayList<String>();
        list.add("first item");
        list.add("second item");
        list.add("third item");

        ListFormatter formatter = new ListFormatter(",", '\'');
        formatter.addStringList(list);
        String formattedString = formatter.getFormattedString();
        assertEquals("comma delimited with spaces", "'first item','second item','third item'", formattedString);
    }

}
