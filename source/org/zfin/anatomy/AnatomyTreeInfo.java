package org.zfin.anatomy;

import org.zfin.util.EqualsUtil;

import java.io.Serializable;


/**
 * This objects contains info about ao terms and their position in
 * the DAG. A very simplified version that needs to be reworked when
 * we implement a generic DAG.
 */
public class AnatomyTreeInfo implements Serializable {

    private String zdbID;
    private int sequenceNumber;
    private int indent;
    private AnatomyItem item;
    private AnatomyStatistics statistics;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getIndent() {
        return indent;
    }

    public void setIndent(int indent) {
        this.indent = indent;
    }

    public AnatomyItem getItem() {
        return item;
    }

    public void setItem(AnatomyItem item) {
        this.item = item;
    }

    public AnatomyStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(AnatomyStatistics statistics) {
        this.statistics = statistics;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof AnatomyTreeInfo))
            return false;

        AnatomyTreeInfo itemOne = (AnatomyTreeInfo) o;

        return EqualsUtil.areEqual(sequenceNumber, itemOne.getSequenceNumber()) &&
                EqualsUtil.areEqual(indent, itemOne.getIndent()) &&
                EqualsUtil.areEqual(item, itemOne.getItem()) &&
                EqualsUtil.areEqual(statistics, itemOne.getStatistics());
    }

    @Override
    public int hashCode() {

        int hash = 7;
        hash = 31 * hash + sequenceNumber;
        hash = 31 * hash + indent;
        hash = 31 * hash + (null == item ? 0 : item.hashCode());
        hash = 31 * hash + (null == statistics ? 0 : statistics.hashCode());
        return hash;
    }

}
