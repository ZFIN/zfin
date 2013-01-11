package org.zfin.infrastructure;

import java.io.Serializable;

/**
 * This class holds a replaced zdbID and its replacement ZDBID including the
 * old name.
 */
public class WithdrawnZdbID implements Serializable {

    private String wdoldZdbID;
    private String wdnewZdbID;

    public String getWdoldZdbID() {
        return wdoldZdbID;
    }

    public void setWdoldZdbID(String wdoldZdbID) {
        this.wdoldZdbID = wdoldZdbID;
    }

    public String getWdnewZdbID() {
        return wdnewZdbID;
    }

    public void setWdnewZdbID(String wdnewZdbID) {
        this.wdnewZdbID = wdnewZdbID;
    }

    public String getCompositeKey() {
        return wdoldZdbID + wdnewZdbID;
    }

    public boolean equals(Object o) {
        if (!(o instanceof WithdrawnZdbID))
            return false;

        WithdrawnZdbID wdnewZdbID = (WithdrawnZdbID) o;
        return wdnewZdbID.getCompositeKey().equals(this.getCompositeKey());
    }

    public int hashCode() {
        return getCompositeKey().hashCode();
    }

}
