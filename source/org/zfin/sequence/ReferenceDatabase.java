/**
 *  Class ReferenceDatabase.
 */
package org.zfin.sequence;

import org.zfin.sequence.blast.Database;

import java.util.*;

public class ReferenceDatabase implements Comparable<ReferenceDatabase> {

    private String zdbID;
    private ForeignDB foreignDB;
    private String organism;
    private ForeignDBDataType foreignDBDataType;
    private Database primaryBlastDatabase;
    private Set<DisplayGroup> displayGroups;
    private List<Database> relatedBlastDbs;

    public List<Database> getRelatedBlastDbs() {
        return relatedBlastDbs;
    }

    public void setRelatedBlastDbs(List<Database> relatedBlastDbs) {
        this.relatedBlastDbs = relatedBlastDbs;
    }

    public ForeignDBDataType getForeignDBDataType() {
        return foreignDBDataType;
    }

    public void setForeignDBDataType(ForeignDBDataType foreignDBDataType) {
        this.foreignDBDataType = foreignDBDataType;
    }

    public ForeignDB getForeignDB() {
        return foreignDB;
    }

    public void setForeignDB(ForeignDB foreignDB) {
        this.foreignDB = foreignDB;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getBaseURL() {
        return foreignDB.getDbUrlPrefix();
    }

    public void setBaseURL(String baseURL) {
        foreignDB.setDbUrlPrefix(baseURL);
    }

    public String getOrganism() {
        return organism;
    }

    public void setOrganism(String organism) {
        this.organism = organism;
    }

    public Database getPrimaryBlastDatabase() {
        return primaryBlastDatabase;
    }

    public void setPrimaryBlastDatabase(Database primaryBlastDatabase) {
        this.primaryBlastDatabase = primaryBlastDatabase;
    }

    public Set<DisplayGroup> getDisplayGroups() {
        return displayGroups;
    }

    public void setDisplayGroups(Set<DisplayGroup> displayGroups) {
        this.displayGroups = displayGroups;
    }

    public String toString() {
        String returnString = "";
        returnString += zdbID + " ";
        returnString += organism + " ";
        if (foreignDB != null) {
            returnString += foreignDB.getDbName() + " ";
        }
        if (primaryBlastDatabase != null) {
            returnString += primaryBlastDatabase.toString();
        }
        return returnString;
    }

    public int compareTo(ReferenceDatabase otherRefDB) {
        if (otherRefDB == null) return +1;
        return foreignDB.getDbName().compareTo(otherRefDB.getForeignDB().getDbName());
    }

    public boolean isInDisplayGroup(DisplayGroup.GroupName groupName) {
        for (DisplayGroup displayGroup : getDisplayGroups()) {
            if (displayGroup.getGroupName() == groupName) {
                return true;
            }
        }
        return false;
    }

    public String getView() {
        return foreignDB.getDbName().toString() + " " + foreignDBDataType.getDataType() + " " + foreignDBDataType.getSuperType();
    }

    public List<Database> getOrderedRelatedBlastDB() {
        List<Database> sortedDatabases = new ArrayList<Database>();
        sortedDatabases.addAll(relatedBlastDbs);
        Collections.sort(sortedDatabases, new Comparator<Database>() {
            public int compare(Database o1, Database o2) {
                if (o1.getToolDisplayOrder() != null && o2.getToolDisplayOrder() != null) {
                    return o1.getToolDisplayOrder().compareTo(o2.getToolDisplayOrder());
                } else if (o1.getToolDisplayOrder() == null && o2.getToolDisplayOrder() == null) {
                    return o1.getName().compareTo(o2.getName());
                } else if (o1.getToolDisplayOrder() == null && o2.getToolDisplayOrder() != null) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return sortedDatabases;
    }

    public boolean isShortSequence() {
        String abbrevString = foreignDB.getDbName().toString();
        return abbrevString.contains("miRNA") ||
                abbrevString.contains("miRBASE")
                ;
    }
}


