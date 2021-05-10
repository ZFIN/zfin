package org.zfin.gwt.root.ui;

/**
* Created by IntelliJ IDEA.
* User: nathandunn
* Date: Oct 8, 2010
* Time: 9:04:08 AM
* To change this template use File | Settings | File Templates.
*/
public enum CssStyles {

    EVEN("even"),
    ODD("odd"),
    EVENGROUP("evengroup"),
    ODDGROUP("oddgroup"),
    NEWGROUP("newgroup"),
    OLDGROUP("oldgroup"),
    EXPERIMENT_ROW("experiment-row"),
    SEARCHRESULTS("searchresults"),
    GROUPSTRIPES_HOVER("groupstripes-hover"),
    TABLE_HEADER("table-header");

    private final String value;

    CssStyles(String name) {
        this.value = name;
    }

    public String toString() {
        return this.value;
    }

    public static CssStyles getType(String type) {
        for (CssStyles t : values()) {
            if (t.toString().equals(type))
                return t;
        }
        throw new RuntimeException("No Css class named " + type + " found.");
    }

}
