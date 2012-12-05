package org.zfin.mutant;

/**
 * Created with IntelliJ IDEA.
 * User: Prita
 * Date: 8/3/12

 * Time: 3:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConstructSearch {
    private long ID;
    private int phenotypeFigureCount;

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getConstructID() {
        return ConstructID;
    }

    public void setConstructID(String constructID) {
        ConstructID = constructID;
    }

    public String getConstructName() {
        return constructName;
    }

    public String getConstructAbbrev() {
        return constructAbbrev;
    }

    public void setConstructAbbrev(String constructAbbrev) {
        this.constructAbbrev = constructAbbrev;
    }

    public void setConstructName(String constructName) {
        this.constructName = constructName;
    }

    private String ConstructID;
    private String constructName;
    private String constructAbbrev;

    public int getPhenotypeFigureCount() {
        return phenotypeFigureCount;
    }

    public void setPhenotypeFigureCount(int phenotypeFigureCount) {
        this.phenotypeFigureCount = phenotypeFigureCount;
    }
}
