package org.zfin.expression;

import org.zfin.anatomy.DevelopmentStage;

import java.io.Serializable;

/**
 * The stage information for the image table is located in another
 * table.  This class is necessary for hibernate to make the join.
 */
public class ImageStage implements Serializable {
    String zdbID;
    DevelopmentStage start;
    DevelopmentStage end;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public DevelopmentStage getStart() {
        return start;
    }

    public void setStart(DevelopmentStage start) {
        this.start = start;
    }

    public DevelopmentStage getEnd() {
        return end;
    }

    public void setEnd(DevelopmentStage end) {
        this.end = end;
    }
}
