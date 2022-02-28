/**
 *  Class ReferenceDatabase.
 */
package org.zfin.sequence;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.sequence.blast.Database;

import java.util.*;

@Setter
@Getter
public class ReferenceDatabase implements Comparable<ReferenceDatabase> {

    private String zdbID;
    @JsonView(View.MarkerRelationshipAPI.class)
    private ForeignDB foreignDB;
    private String organism;
    private ForeignDBDataType foreignDBDataType;
    private Database primaryBlastDatabase;
    private Set<DisplayGroup> displayGroups;
    private List<Database> relatedBlastDbs;

    public String getBaseURL() {
        return foreignDB.getDbUrlPrefix();
    }

    public void setBaseURL(String baseURL) {
        foreignDB.setDbUrlPrefix(baseURL);
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

        if (foreignDB.compareTo(otherRefDB.getForeignDB()) != 0)
            return foreignDB.compareTo(otherRefDB.getForeignDB());

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
        List<Database> sortedDatabases = new ArrayList<>();
        sortedDatabases.addAll(relatedBlastDbs);
        Collections.sort(sortedDatabases, (o1, o2) -> {
            if (o1.getToolDisplayOrder() != null && o2.getToolDisplayOrder() != null) {
                return o1.getToolDisplayOrder().compareTo(o2.getToolDisplayOrder());
            } else if (o1.getToolDisplayOrder() == null && o2.getToolDisplayOrder() == null) {
                return o1.getName().compareTo(o2.getName());
            } else if (o1.getToolDisplayOrder() == null && o2.getToolDisplayOrder() != null) {
                return 1;
            } else {
                return -1;
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

    public boolean isRefSeq() {
        // would better put the hard=coded ids to somewhere else as Enum values
        return (getZdbID().equals("ZDB-FDBCONT-040412-38") || getZdbID().equals("ZDB-FDBCONT-040412-39") || getZdbID().equals("ZDB-FDBCONT-040527-1"));
    }
}


