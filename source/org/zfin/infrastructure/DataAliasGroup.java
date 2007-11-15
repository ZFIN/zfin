package org.zfin.infrastructure;

/**
 * This class is only used for validation of the correct
 * group enumeration items.
 * Use DataAlias.Group enum if needed.
 */
public class DataAliasGroup {

    private String name;
    private int significance;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSignificance() {
        return significance;
    }

    public void setSignificance(int significance) {
        this.significance = significance;
    }
}
