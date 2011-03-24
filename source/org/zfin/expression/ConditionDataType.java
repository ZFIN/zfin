package org.zfin.expression;

/**
 * ToDo: Please add documentation for this class.
 */
public class ConditionDataType implements Comparable<ConditionDataType> {

    private String zdbID;
    private String name;
    private String group;
    private int significance;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getSignificance() {
        return significance;
    }

    public void setSignificance(int significance) {
        this.significance = significance;
    }


    @Override
    public int compareTo(ConditionDataType o) {
        Integer thisSignificance = new Integer(significance);
        Integer thatSignificance = new Integer(o.getSignificance());

        return thisSignificance.compareTo(thatSignificance);

    }
}
