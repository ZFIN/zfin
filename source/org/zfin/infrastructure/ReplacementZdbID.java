package org.zfin.infrastructure;

import java.io.Serializable;

/**
 * This class holds a replaced zdbID and its replacement ZDBID including the
 * old name.
 */
public class ReplacementZdbID implements Serializable {

    private String oldZdbID;
    private String replacementZdbID;
    private String oldName;

    public String getOldZdbID() {
        return oldZdbID;
    }

    public void setOldZdbID(String oldZdbID) {
        this.oldZdbID = oldZdbID;
    }

    public String getReplacementZdbID() {
        return replacementZdbID;
    }

    public void setReplacementZdbID(String replacementZdbID) {
        this.replacementZdbID = replacementZdbID;
    }

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public String getCompositeKey() {
        return oldZdbID + replacementZdbID;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ReplacementZdbID))
            return false;

        ReplacementZdbID replacementZdbID = (ReplacementZdbID) o;
        return replacementZdbID.getCompositeKey().equals(this.getCompositeKey());
    }

    public int hashCode() {
        return getCompositeKey().hashCode();
    }

}
